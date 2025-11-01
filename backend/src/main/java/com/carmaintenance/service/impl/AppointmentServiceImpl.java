package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.dto.AppointmentDTO;
import com.carmaintenance.dto.AppointmentItemDTO;
import com.carmaintenance.entity.*;
import com.carmaintenance.exception.BusinessException;
import com.carmaintenance.repository.*;
import com.carmaintenance.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 预约Service实现类
 */
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentRepository, Appointment> implements AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    @Autowired
    private AppointmentItemRepository appointmentItemRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    @Transactional
    public String createAppointment(AppointmentDTO appointmentDTO, Long userId) {
        logger.info("用户创建预约: userId={}, shopId={}, vehicleId={}, date={}", 
                   userId, appointmentDTO.getShopId(), appointmentDTO.getVehicleId(), appointmentDTO.getAppointmentDate());
        
        // 检查门店是否存在
        Shop shop = shopRepository.selectById(appointmentDTO.getShopId());
        if (shop == null || shop.getStatus() != 1) {
            logger.warn("预约创建失败: 门店不存在或已停业, shopId={}", appointmentDTO.getShopId());
            throw new BusinessException(400, "门店不存在或已停业");
        }

        // 检查车辆是否属于用户
        Vehicle vehicle = vehicleRepository.selectById(appointmentDTO.getVehicleId());
        if (vehicle == null || !vehicle.getUserId().equals(userId)) {
            logger.warn("预约创建失败: 车辆不存在或无权操作, userId={}, vehicleId={}", userId, appointmentDTO.getVehicleId());
            throw new BusinessException(400, "车辆不存在或无权操作");
        }

        // 检查预约日期是否在可预约范围内（未来7天）
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(7);
        if (appointmentDTO.getAppointmentDate().isBefore(today) ||
            appointmentDTO.getAppointmentDate().isAfter(maxDate)) {
            logger.warn("预约创建失败: 预约日期超出范围, date={}", appointmentDTO.getAppointmentDate());
            throw new BusinessException(400, "预约日期必须在今天到未来7天内");
        }

        // 检查时间段是否可用
        if (!checkTimeSlotAvailable(appointmentDTO.getShopId(),
                                  appointmentDTO.getAppointmentDate(),
                                  appointmentDTO.getTimeSlot())) {
            logger.warn("预约创建失败: 时间段已被预约, shopId={}, date={}, timeSlot={}", 
                       appointmentDTO.getShopId(), appointmentDTO.getAppointmentDate(), appointmentDTO.getTimeSlot());
            throw new BusinessException(400, "该时间段已被预约");
        }

        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (appointmentDTO.getItems() != null && !appointmentDTO.getItems().isEmpty()) {
            for (AppointmentItemDTO itemDTO : appointmentDTO.getItems()) {
                BigDecimal subtotal = itemDTO.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
                totalAmount = totalAmount.add(subtotal);
            }
        }

        // 创建预约
        Appointment appointment = new Appointment();
        appointment.setAppointmentNo(generateAppointmentNo());
        appointment.setUserId(userId);
        appointment.setShopId(appointmentDTO.getShopId());
        appointment.setVehicleId(appointmentDTO.getVehicleId());
        appointment.setAppointmentDate(appointmentDTO.getAppointmentDate());
        appointment.setTimeSlot(appointmentDTO.getTimeSlot());
        appointment.setBayNumber(appointmentDTO.getBayNumber());
        appointment.setTotalAmount(totalAmount);
        appointment.setStatus(Appointment.Status.PENDING.getCode());

        if (save(appointment)) {
            // 创建预约项目
            if (appointmentDTO.getItems() != null && !appointmentDTO.getItems().isEmpty()) {
                List<AppointmentItem> items = new ArrayList<>();
                for (AppointmentItemDTO itemDTO : appointmentDTO.getItems()) {
                    AppointmentItem item = new AppointmentItem();
                    item.setAppointmentId(appointment.getId());
                    item.setItemId(itemDTO.getItemId());
                    item.setPackageId(itemDTO.getPackageId());
                    item.setItemName(itemDTO.getItemName());
                    item.setPrice(itemDTO.getPrice());
                    item.setQuantity(itemDTO.getQuantity());
                    item.setSubtotal(itemDTO.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
                    items.add(item);
                }
                appointmentItemRepository.insertBatch(items);
            }

            // 自动创建订单
            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setAppointmentId(appointment.getId());
            order.setUserId(userId);
            order.setShopId(appointmentDTO.getShopId());
            order.setVehicleId(appointmentDTO.getVehicleId()); // 添加车辆ID
            order.setTotalAmount(totalAmount);
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setFinalAmount(totalAmount);
            order.setPaymentMethod(1); // 默认线下支付
            order.setPaymentStatus(0); // 未支付
            order.setStatus(1); // 待支付
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            orderRepository.insert(order);

            logger.info("预约创建成功: userId={}, appointmentId={}, appointmentNo={}, orderId={}",
                       userId, appointment.getId(), appointment.getAppointmentNo(), order.getId());
            return "预约成功，预约编号：" + appointment.getAppointmentNo();
        } else {
            logger.error("预约创建失败: 数据库保存失败, userId={}", userId);
            throw new BusinessException(500, "预约创建失败");
        }
    }

    @Override
    @Transactional
    public boolean cancelAppointment(Long appointmentId, Long userId, String reason) {
        Appointment appointment = getById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(404, "预约不存在");
        }

        // 检查权限
        if (!appointment.getUserId().equals(userId)) {
            logger.warn("预约取消失败: 无权操作, userId={}, appointmentId={}", userId, appointmentId);
            throw new BusinessException(403, "无权操作该预约");
        }

        // 检查状态
        if (appointment.getStatus() != Appointment.Status.PENDING.getCode()) {
            logger.warn("预约取消失败: 状态不允许, appointmentId={}, status={}", appointmentId, appointment.getStatus());
            throw new BusinessException(400, "只能取消待服务的预约");
        }

        // 更新预约状态
        appointment.setStatus(Appointment.Status.CANCELLED.getCode());
        appointment.setCancelReason(reason);
        appointment.setCancelTime(LocalDateTime.now());

        return updateById(appointment);
    }

    @Override
    @Transactional
    public boolean updateAppointment(Long appointmentId, AppointmentDTO appointmentDTO, Long userId) {
        Appointment appointment = getById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        // 检查权限
        if (!appointment.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该预约");
        }

        // 检查状态
        if (appointment.getStatus() != Appointment.Status.PENDING.getCode()) {
            throw new RuntimeException("只能修改待服务的预约");
        }

        // 检查新的时间段是否可用
        if (!appointment.getTimeSlot().equals(appointmentDTO.getTimeSlot()) ||
            !appointment.getAppointmentDate().equals(appointmentDTO.getAppointmentDate())) {

            if (!checkTimeSlotAvailable(appointmentDTO.getShopId(),
                                      appointmentDTO.getAppointmentDate(),
                                      appointmentDTO.getTimeSlot())) {
                throw new RuntimeException("该时间段已被预约");
            }
        }

        // 更新预约信息
        appointment.setVehicleId(appointmentDTO.getVehicleId());
        appointment.setAppointmentDate(appointmentDTO.getAppointmentDate());
        appointment.setTimeSlot(appointmentDTO.getTimeSlot());
        appointment.setBayNumber(appointmentDTO.getBayNumber());

        // 重新计算总金额
        if (appointmentDTO.getItems() != null && !appointmentDTO.getItems().isEmpty()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (AppointmentItemDTO itemDTO : appointmentDTO.getItems()) {
                BigDecimal subtotal = itemDTO.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
                totalAmount = totalAmount.add(subtotal);
            }
            appointment.setTotalAmount(totalAmount);

            // 删除原有项目并重新创建
            appointmentItemRepository.delete(new QueryWrapper<AppointmentItem>()
                .eq("appointment_id", appointmentId));

            List<AppointmentItem> items = new ArrayList<>();
            for (AppointmentItemDTO itemDTO : appointmentDTO.getItems()) {
                AppointmentItem item = new AppointmentItem();
                item.setAppointmentId(appointment.getId());
                item.setItemId(itemDTO.getItemId());
                item.setPackageId(itemDTO.getPackageId());
                item.setItemName(itemDTO.getItemName());
                item.setPrice(itemDTO.getPrice());
                item.setQuantity(itemDTO.getQuantity());
                item.setSubtotal(itemDTO.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
                items.add(item);
            }
            appointmentItemRepository.insertBatch(items);
        }

        return updateById(appointment);
    }

    @Override
    public Page<Appointment> getUserAppointments(Page<Appointment> page, Long userId, Integer status) {
        QueryWrapper<Appointment> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);

        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("create_time");
        return page(page, wrapper);
    }

    @Override
    public Page<Appointment> getShopAppointments(Page<Appointment> page, Long shopId, Integer status, LocalDate date) {
        QueryWrapper<Appointment> wrapper = new QueryWrapper<>();
        wrapper.eq("shop_id", shopId);

        if (status != null) {
            wrapper.eq("status", status);
        }

        if (date != null) {
            wrapper.eq("appointment_date", date);
        }

        wrapper.orderByDesc("create_time");
        return page(page, wrapper);
    }

    @Override
    public Map<String, Object> getAppointmentDetail(Long appointmentId, Long userId) {
        Appointment appointment = getById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        // 检查权限（用户或门店）
        if (!appointment.getUserId().equals(userId)) {
            // 如果不是用户，检查是否为门店用户
            // 这里可以添加门店权限检查逻辑
        }

        // 获取预约项目
        List<AppointmentItem> items = appointmentItemRepository.findByAppointmentId(appointmentId);

        // 获取车辆信息
        Vehicle vehicle = vehicleRepository.selectById(appointment.getVehicleId());

        // 获取门店信息
        Shop shop = shopRepository.selectById(appointment.getShopId());

        Map<String, Object> result = new HashMap<>();
        result.put("appointment", appointment);
        result.put("items", items);
        result.put("vehicle", vehicle);
        result.put("shop", shop);

        return result;
    }

    @Override
    public boolean checkTimeSlotAvailable(Long shopId, LocalDate date, String timeSlot) {
        int count = baseMapper.countByShopIdAndDateTime(shopId, date, timeSlot);
        Shop shop = shopRepository.selectById(shopId);
        int maxBays = shop.getShopBays() != null ? shop.getShopBays() : 10; // 默认10个工位
        return count < maxBays;
    }

    @Override
    public List<String> getAvailableTimeSlots(Long shopId, LocalDate date) {
        // 生成时间段列表（08:00-18:00，每30分钟一个时段）
        List<String> allTimeSlots = new ArrayList<>();
        for (int hour = 8; hour < 18; hour++) {
            allTimeSlots.add(String.format("%02d:00-%02d:30", hour, hour));
            allTimeSlots.add(String.format("%02d:30-%02d:00", hour, hour + 1));
        }

        // 获取已被预约的时间段
        List<Appointment> bookedAppointments = baseMapper.findByShopIdAndDate(shopId, date);
        Set<String> bookedTimeSlots = bookedAppointments.stream()
            .map(Appointment::getTimeSlot)
            .collect(Collectors.toSet());

        // 返回可用时间段
        return allTimeSlots.stream()
            .filter(slot -> !bookedTimeSlots.contains(slot))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean updateAppointmentStatus(Long appointmentId, Integer status) {
        Appointment appointment = getById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        appointment.setStatus(status);
        return updateById(appointment);
    }

    @Override
    @Transactional
    public boolean assignBay(Long appointmentId, Integer bayNumber) {
        Appointment appointment = getById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        appointment.setBayNumber(bayNumber);
        return updateById(appointment);
    }

    @Override
    public Map<String, Object> getAppointmentStats(Long shopId) {
        Map<String, Object> stats = new HashMap<>();

        // 总预约数
        long totalCount = count(new QueryWrapper<Appointment>().eq("shop_id", shopId));
        stats.put("totalCount", totalCount);

        // 各状态预约数
        stats.put("pendingCount", count(new QueryWrapper<Appointment>()
            .eq("shop_id", shopId).eq("status", Appointment.Status.PENDING.getCode())));
        stats.put("inProgressCount", count(new QueryWrapper<Appointment>()
            .eq("shop_id", shopId).eq("status", Appointment.Status.IN_PROGRESS.getCode())));
        stats.put("completedCount", count(new QueryWrapper<Appointment>()
            .eq("shop_id", shopId).eq("status", Appointment.Status.COMPLETED.getCode())));
        stats.put("cancelledCount", count(new QueryWrapper<Appointment>()
            .eq("shop_id", shopId).eq("status", Appointment.Status.CANCELLED.getCode())));

        // 今日预约数
        stats.put("todayCount", count(new QueryWrapper<Appointment>()
            .eq("shop_id", shopId).eq("appointment_date", LocalDate.now())));

        return stats;
    }

    /**
     * 生成预约编号
     */
    private String generateAppointmentNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "APT" + LocalDateTime.now().format(formatter) +
               String.format("%03d", new Random().nextInt(1000));
    }

    private String generateOrderNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "ORD" + LocalDateTime.now().format(formatter) +
               String.format("%03d", new Random().nextInt(1000));
    }
}