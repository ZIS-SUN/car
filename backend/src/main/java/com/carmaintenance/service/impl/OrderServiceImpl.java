package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.dto.OrderDTO;
import com.carmaintenance.dto.OrderVO;
import com.carmaintenance.entity.*;
import com.carmaintenance.exception.BusinessException;
import com.carmaintenance.repository.*;
import com.carmaintenance.service.AppointmentService;
import com.carmaintenance.service.OrderService;
import com.carmaintenance.service.UserMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 订单Service实现类
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderRepository, Order> implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentItemRepository appointmentItemRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserMemberService userMemberService;

    @Override
    @Transactional
    public String createOrder(OrderDTO orderDTO, Long userId) {
        logger.info("用户创建订单: userId={}, appointmentId={}", userId, orderDTO.getAppointmentId());
        
        // 获取预约信息
        Appointment appointment = appointmentRepository.selectById(orderDTO.getAppointmentId());
        if (appointment == null) {
            logger.warn("订单创建失败: 预约不存在, appointmentId={}", orderDTO.getAppointmentId());
            throw new BusinessException(404, "预约不存在");
        }

        // 检查权限
        if (!appointment.getUserId().equals(userId)) {
            logger.warn("订单创建失败: 无权操作预约, userId={}, appointmentId={}", userId, orderDTO.getAppointmentId());
            throw new BusinessException(403, "无权操作该预约");
        }

        // 检查预约状态
        if (appointment.getStatus() != Appointment.Status.PENDING.getCode()) {
            logger.warn("订单创建失败: 预约状态不允许, appointmentId={}, status={}", orderDTO.getAppointmentId(), appointment.getStatus());
            throw new BusinessException(400, "只能为待服务的预约创建订单");
        }

        // 检查是否已存在订单
        Order existingOrder = ((OrderRepository) getBaseMapper()).findByAppointmentId(orderDTO.getAppointmentId());
        if (existingOrder != null) {
            logger.warn("订单创建失败: 预约已存在订单, appointmentId={}, existingOrderId={}", orderDTO.getAppointmentId(), existingOrder.getId());
            throw new BusinessException(400, "该预约已存在订单");
        }

        // 计算订单金额
        BigDecimal totalAmount = appointment.getTotalAmount();
        BigDecimal discountAmount = orderDTO.getDiscountAmount() != null ? orderDTO.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setAppointmentId(orderDTO.getAppointmentId());
        order.setUserId(appointment.getUserId());
        order.setShopId(appointment.getShopId());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setFinalAmount(finalAmount);
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setPaymentStatus(0); // 未支付
        order.setTechnicianId(orderDTO.getTechnicianId());
        order.setStatus(Order.Status.PENDING.getCode());

        if (save(order)) {
            // 更新预约状态为进行中
            appointment.setStatus(Appointment.Status.IN_PROGRESS.getCode());
            appointmentRepository.updateById(appointment);

            logger.info("订单创建成功: userId={}, orderId={}, orderNo={}", userId, order.getId(), order.getOrderNo());
            return "订单创建成功，订单编号：" + order.getOrderNo();
        } else {
            logger.error("订单创建失败: 数据库保存失败, userId={}, appointmentId={}", userId, orderDTO.getAppointmentId());
            throw new BusinessException(500, "订单创建失败");
        }
    }

    @Override
    @Transactional
    public boolean payOrder(Long orderId, Integer paymentMethod) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        if (order.getPaymentStatus() == 1) {
            logger.warn("订单支付失败: 订单已支付, orderId={}", orderId);
            throw new BusinessException(400, "订单已支付");
        }

        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(1); // 已支付
        order.setStatus(Order.Status.IN_PROGRESS.getCode()); // 更新订单状态为进行中（已支付待服务）

        logger.info("订单支付成功: orderId={}, paymentMethod={}, status={}", orderId, paymentMethod, order.getStatus());
        
        return updateById(order);
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long orderId, Long userId, String reason) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 检查权限
        if (!order.getUserId().equals(userId)) {
            logger.warn("订单取消失败: 无权操作, userId={}, orderId={}", userId, orderId);
            throw new BusinessException(403, "无权操作该订单");
        }

        // 检查状态
        if (order.getStatus() == Order.Status.COMPLETED.getCode()) {
            logger.warn("订单取消失败: 订单已完成, orderId={}", orderId);
            throw new BusinessException(400, "已完成的订单不能取消");
        }

        // 更新订单状态
        order.setStatus(Order.Status.CANCELLED.getCode());

        // 更新预约状态
        Appointment appointment = appointmentRepository.selectById(order.getAppointmentId());
        if (appointment != null) {
            appointment.setStatus(Appointment.Status.CANCELLED.getCode());
            appointment.setCancelReason(reason);
            appointment.setCancelTime(LocalDateTime.now());
            appointmentRepository.updateById(appointment);
        }

        return updateById(order);
    }

    @Override
    @Transactional
    public boolean startService(Long orderId, Long technicianId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        if (order.getStatus() != Order.Status.PENDING.getCode()) {
            logger.warn("订单开始服务失败: 状态不允许, orderId={}, status={}", orderId, order.getStatus());
            throw new BusinessException(400, "只能开始待服务的订单");
        }

        order.setStatus(Order.Status.IN_PROGRESS.getCode());
        order.setTechnicianId(technicianId);
        order.setStartTime(LocalDateTime.now());

        return updateById(order);
    }

    @Override
    @Transactional
    public boolean completeService(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        if (order.getStatus() != Order.Status.IN_PROGRESS.getCode()) {
            logger.warn("订单完成失败: 状态不允许, orderId={}, status={}", orderId, order.getStatus());
            throw new BusinessException(400, "只能完成进行中的订单");
        }

        order.setStatus(Order.Status.COMPLETED.getCode());
        order.setEndTime(LocalDateTime.now());

        // 更新预约状态
        Appointment appointment = appointmentRepository.selectById(order.getAppointmentId());
        if (appointment != null) {
            appointment.setStatus(Appointment.Status.COMPLETED.getCode());
            appointmentRepository.updateById(appointment);
        }

        // 为用户添加经验值奖励
        userMemberService.addExperienceForOrder(order.getUserId(), order.getFinalAmount(), order.getId());

        return updateById(order);
    }

    @Override
    public Page<Order> getUserOrders(Page<Order> page, Long userId, Integer status) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);

        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("create_time");
        return page(page, wrapper);
    }

    @Override
    public Page<Order> getShopOrders(Page<Order> page, Long shopId, Integer status) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("shop_id", shopId);

        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("create_time");
        return page(page, wrapper);
    }

    @Override
    public Map<String, Object> getOrderDetail(Long orderId, Long userId, Integer userType) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        boolean isOwner = order.getUserId().equals(userId);
        boolean isAdmin = userType != null && User.UserType.ADMIN.getCode().equals(userType);
        boolean isShopOwner = false;

        if (!isOwner && !isAdmin) {
            Shop shop = shopRepository.findByUserId(userId);
            if (shop != null && shop.getId().equals(order.getShopId())) {
                isShopOwner = true;
            }
        }

        if (!isOwner && !isAdmin && !isShopOwner) {
            throw new BusinessException(403, "无权查看该订单");
        }

        // 获取预约信息
        Appointment appointment = appointmentRepository.selectById(order.getAppointmentId());

        // 获取预约项目
        List<AppointmentItem> items = appointmentItemRepository.findByAppointmentId(order.getAppointmentId());

        // 获取车辆信息
        Vehicle vehicle = vehicleRepository.selectById(appointment.getVehicleId());

        // 获取门店信息
        Shop shop = shopRepository.selectById(order.getShopId());

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("appointment", appointment);
        result.put("items", items);
        result.put("vehicle", vehicle);
        result.put("shop", shop);

        return result;
    }

    @Override
    @Transactional
    public boolean updateOrderStatus(Long orderId, Integer status) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        order.setStatus(status);
        return updateById(order);
    }

    @Override
    @Transactional
    public boolean assignTechnician(Long orderId, Long technicianId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        order.setTechnicianId(technicianId);
        return updateById(order);
    }

    @Override
    public Map<String, Object> getOrderStats(Long shopId) {
        Map<String, Object> stats = new HashMap<>();

        // 总订单数
        long totalCount = count(new QueryWrapper<Order>().eq("shop_id", shopId));
        stats.put("totalCount", totalCount);

        // 各状态订单数
        stats.put("pendingCount", count(new QueryWrapper<Order>()
            .eq("shop_id", shopId).eq("status", Order.Status.PENDING.getCode())));
        stats.put("inProgressCount", count(new QueryWrapper<Order>()
            .eq("shop_id", shopId).eq("status", Order.Status.IN_PROGRESS.getCode())));
        stats.put("completedCount", count(new QueryWrapper<Order>()
            .eq("shop_id", shopId).eq("status", Order.Status.COMPLETED.getCode())));
        stats.put("cancelledCount", count(new QueryWrapper<Order>()
            .eq("shop_id", shopId).eq("status", Order.Status.CANCELLED.getCode())));

        // 今日订单数
        stats.put("todayCount", count(new QueryWrapper<Order>()
            .eq("shop_id", shopId)
            .apply("DATE(create_time) = CURDATE()")));

        // 营业额统计
        Map<String, Object> revenue = new HashMap<>();
        revenue.put("totalRevenue", getBaseMapper().selectList(
            new QueryWrapper<Order>().eq("shop_id", shopId).eq("status", Order.Status.COMPLETED.getCode())
        ).stream().mapToDouble(order -> order.getFinalAmount().doubleValue()).sum());

        revenue.put("todayRevenue", getBaseMapper().selectList(
            new QueryWrapper<Order>()
                .eq("shop_id", shopId)
                .eq("status", Order.Status.COMPLETED.getCode())
                .apply("DATE(create_time) = CURDATE()")
        ).stream().mapToDouble(order -> order.getFinalAmount().doubleValue()).sum());

        stats.put("revenue", revenue);

        return stats;
    }

    @Override
    public List<OrderVO> getUserOrdersWithDetails(Long userId, Integer status) {
        return ((OrderRepository) getBaseMapper()).findUserOrdersWithDetails(userId, status);
    }

    @Override
    public List<OrderVO> getShopOrdersWithDetails(Long shopId, Integer status) {
        return ((OrderRepository) getBaseMapper()).findShopOrdersWithDetails(shopId, status);
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "ORD" + LocalDateTime.now().format(formatter) +
               String.format("%03d", new Random().nextInt(1000));
    }
}
