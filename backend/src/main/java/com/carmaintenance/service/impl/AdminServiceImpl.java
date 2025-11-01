package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.entity.*;
import com.carmaintenance.exception.BusinessException;
import com.carmaintenance.repository.*;
import com.carmaintenance.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 管理员Service实现类
 */
@Service
public class AdminServiceImpl extends ServiceImpl<SystemConfigRepository, SystemConfig> implements AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserMemberRepository userMemberRepository;

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @Override
    public Page<User> getUserList(Page<User> page, Integer userType, String keyword, String status) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        if (userType != null) {
            wrapper.eq("user_type", userType);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("username", keyword)
                    .or().like("real_name", keyword)
                    .or().like("email", keyword)
                    .or().like("phone", keyword));
        }

        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("create_time");

        return userRepository.selectPage(page, wrapper);
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        user.setStatus(status);
        boolean result = userRepository.updateById(user) > 0;
        if (result) {
            logger.info("管理员更新用户状态成功: userId={}, status={}", userId, status);
        }
        return result;
    }

    @Override
    public boolean deleteUser(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        boolean result = userRepository.deleteById(userId) > 0;
        if (result) {
            logger.info("管理员删除用户成功: userId={}", userId);
        }
        return result;
    }

    @Override
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();

        // 总用户数
        long totalUsers = userRepository.selectCount(null);
        stats.put("totalUsers", totalUsers);

        // 各类型用户数
        Map<String, Long> userTypeStats = new HashMap<>();
        for (int i = 1; i <= 3; i++) {
            long count = userRepository.selectCount(new QueryWrapper<User>().eq("user_type", i));
            String typeName = i == 1 ? "车主" : i == 2 ? "门店" : "管理员";
            userTypeStats.put(typeName, count);
        }
        stats.put("userTypeStats", userTypeStats);

        // 今日注册用户数
        long todayUsers = userRepository.selectCount(new QueryWrapper<User>()
                .apply("DATE(create_time) = CURDATE()"));
        stats.put("todayUsers", todayUsers);

        // 活跃用户数（有订单的用户）
        long activeUsers = userRepository.selectCount(new QueryWrapper<User>()
                .in("id", "SELECT DISTINCT user_id FROM orders"));
        stats.put("activeUsers", activeUsers);

        return stats;
    }

    @Override
    public Page<Shop> getShopList(Page<Shop> page, String city, String keyword, Integer status) {
        QueryWrapper<Shop> wrapper = new QueryWrapper<>();

        if (city != null && !city.trim().isEmpty()) {
            wrapper.eq("city", city);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("shop_name", keyword)
                    .or().like("address", keyword));
        }

        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("create_time");

        return shopRepository.selectPage(page, wrapper);
    }

    @Override
    public boolean approveShop(Long shopId, Integer status) {
        Shop shop = shopRepository.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(404, "门店不存在");
        }

        shop.setStatus(status);
        boolean result = shopRepository.updateById(shop) > 0;

        if (result) {
            // 同时更新用户状态
            User user = userRepository.selectById(shop.getUserId());
            if (user != null) {
                user.setStatus(status);
                userRepository.updateById(user);
            }
            logger.info("管理员审核门店成功: shopId={}, status={}", shopId, status);
        }

        return result;
    }

    @Override
    public Map<String, Object> getShopStats() {
        Map<String, Object> stats = new HashMap<>();

        // 总门店数
        long totalShops = shopRepository.selectCount(null);
        stats.put("totalShops", totalShops);

        // 各状态门店数
        Map<String, Long> statusStats = new HashMap<>();
        statusStats.put("待审核", shopRepository.selectCount(new QueryWrapper<Shop>().eq("status", 0)));
        statusStats.put("正常", shopRepository.selectCount(new QueryWrapper<Shop>().eq("status", 1)));
        statusStats.put("已禁用", shopRepository.selectCount(new QueryWrapper<Shop>().eq("status", 2)));
        stats.put("statusStats", statusStats);

        // 城市分布
        List<Shop> shops = shopRepository.selectList(new QueryWrapper<Shop>().eq("status", 1));
        Map<String, Long> cityStats = new HashMap<>();
        for (Shop shop : shops) {
            cityStats.put(shop.getCity(), cityStats.getOrDefault(shop.getCity(), 0L) + 1);
        }
        stats.put("cityStats", cityStats);

        return stats;
    }

    @Override
    public boolean createShop(Shop shop) {
        // 如果没有设置userId，需要创建一个默认的门店用户
        if (shop.getUserId() == null) {
            // 生成一个唯一的用户名
            String username = "shop_" + System.currentTimeMillis();
            User user = new User();
            user.setUsername(username);
            user.setPassword("$2a$10$dummyPasswordHashForShopUser"); // 默认密码哈希，建议管理员后续修改
            user.setEmail(username + "@shop.com");
            user.setPhone(shop.getPhone() != null ? shop.getPhone() : "");
            user.setRealName(shop.getShopName());
            user.setUserType(2); // 门店用户
            user.setStatus(shop.getStatus() != null ? shop.getStatus() : 0); // 状态与门店一致
            
            userRepository.insert(user);
            shop.setUserId(user.getId());
            logger.info("管理员创建门店时自动创建用户: userId={}, username={}", user.getId(), username);
        }
        
        // 设置默认状态为待审核
        if (shop.getStatus() == null) {
            shop.setStatus(0);
        }
        // 设置默认评分（rating字段不存在于数据库中，移除设置）
        // if (shop.getRating() == null) {
        //     shop.setRating(BigDecimal.valueOf(5.0));
        // }
        boolean result = shopRepository.insert(shop) > 0;
        if (result) {
            logger.info("管理员创建门店成功: shopId={}, userId={}", shop.getId(), shop.getUserId());
        }
        return result;
    }

    @Override
    public boolean updateShop(Shop shop) {
        Shop existingShop = shopRepository.selectById(shop.getId());
        if (existingShop == null) {
            throw new BusinessException(404, "门店不存在");
        }
        boolean result = shopRepository.updateById(shop) > 0;
        if (result) {
            logger.info("管理员更新门店成功: shopId={}", shop.getId());
        }
        return result;
    }

    @Override
    public boolean deleteShop(Long shopId) {
        Shop shop = shopRepository.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(404, "门店不存在");
        }
        boolean result = shopRepository.deleteById(shopId) > 0;
        if (result) {
            logger.info("管理员删除门店成功: shopId={}", shopId);
        }
        return result;
    }

    @Override
    public Page<Order> getOrderList(Page<Order> page, Long userId, Long shopId, Integer status,
                                   LocalDate startDate, LocalDate endDate) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();

        if (userId != null) {
            wrapper.eq("user_id", userId);
        }

        if (shopId != null) {
            wrapper.eq("shop_id", shopId);
        }

        if (status != null) {
            wrapper.eq("status", status);
        }

        if (startDate != null) {
            wrapper.ge("create_time", startDate.atStartOfDay());
        }

        if (endDate != null) {
            wrapper.le("create_time", endDate.atTime(23, 59, 59));
        }

        wrapper.orderByDesc("create_time");

        return orderRepository.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getOrderStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (startDate != null) {
            wrapper.ge("create_time", startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le("create_time", endDate.atTime(23, 59, 59));
        }

        // 订单统计
        List<Order> orders = orderRepository.selectList(wrapper);
        stats.put("totalOrders", orders.size());

        // 营业额统计
        BigDecimal totalRevenue = orders.stream()
                .filter(order -> order.getStatus() == Order.Status.COMPLETED.getCode())
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);

        // 各状态订单数
        Map<String, Long> statusStats = new HashMap<>();
        for (Order.Status status : Order.Status.values()) {
            long count = orders.stream()
                    .filter(order -> order.getStatus().equals(status.getCode()))
                    .count();
            statusStats.put(status.getDesc(), count);
        }
        stats.put("statusStats", statusStats);

        // 今日订单
        long todayOrders = orderRepository.selectCount(new QueryWrapper<Order>()
                .apply("DATE(create_time) = CURDATE()"));
        stats.put("todayOrders", todayOrders);

        return stats;
    }

    @Override
    public Map<String, String> getSystemConfigs() {
        Map<String, String> configs = new HashMap<>();

        List<SystemConfig> configList = systemConfigRepository.selectList(null);
        for (SystemConfig config : configList) {
            configs.put(config.getConfigKey(), config.getConfigValue());
        }

        return configs;
    }

    @Override
    public boolean updateSystemConfig(String configKey, String configValue) {
        SystemConfig config = systemConfigRepository.getByConfigKey(configKey);
        if (config != null) {
            return systemConfigRepository.updateConfigValue(configKey, configValue) > 0;
        } else {
            // 创建新配置
            SystemConfig newConfig = new SystemConfig();
            newConfig.setConfigKey(configKey);
            newConfig.setConfigValue(configValue);
            return systemConfigRepository.insert(newConfig) > 0;
        }
    }

    @Override
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        // 基础数据统计
        stats.put("userStats", getUserStats());
        stats.put("shopStats", getShopStats());
        stats.put("orderStats", getOrderStats(null, null));

        // 会员统计
        long totalMembers = userMemberRepository.selectCount(null);
        stats.put("totalMembers", totalMembers);

        // 评价统计
        long totalReviews = reviewRepository.selectCount(new QueryWrapper<Review>().eq("status", 1));
        stats.put("totalReviews", totalReviews);

        // 预约统计
        long totalAppointments = appointmentRepository.selectCount(null);
        stats.put("totalAppointments", totalAppointments);

        // 系统配置
        stats.put("systemConfigs", getSystemConfigs());

        return stats;
    }

    @Override
    public List<Map<String, Object>> exportUsers(Integer userType) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (userType != null) {
            wrapper.eq("user_type", userType);
        }

        List<User> users = userRepository.selectList(wrapper);
        List<Map<String, Object>> exportData = new ArrayList<>();

        for (User user : users) {
            Map<String, Object> data = new HashMap<>();
            data.put("用户ID", user.getId());
            data.put("用户名", user.getUsername());
            data.put("真实姓名", user.getRealName());
            data.put("邮箱", user.getEmail());
            data.put("手机号", user.getPhone());
            data.put("用户类型", User.UserType.fromCode(user.getUserType()).getDesc());
            data.put("状态", user.getStatus() == 1 ? "正常" : "禁用");
            data.put("注册时间", user.getCreateTime() != null ? 
                user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "未知");
            exportData.add(data);
        }

        return exportData;
    }

    @Override
    public List<Map<String, Object>> exportOrders(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (startDate != null) {
            wrapper.ge("create_time", startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le("create_time", endDate.atTime(23, 59, 59));
        }

        List<Order> orders = orderRepository.selectList(wrapper);
        List<Map<String, Object>> exportData = new ArrayList<>();

        for (Order order : orders) {
            Map<String, Object> data = new HashMap<>();
            data.put("订单ID", order.getId());
            data.put("订单编号", order.getOrderNo());
            data.put("用户ID", order.getUserId());
            data.put("门店ID", order.getShopId());
            data.put("总金额", order.getTotalAmount());
            data.put("折扣金额", order.getDiscountAmount());
            data.put("实付金额", order.getFinalAmount());
            data.put("支付方式", getPaymentMethodDesc(order.getPaymentMethod()));
            data.put("支付状态", order.getPaymentStatus() == 1 ? "已支付" : "未支付");
            data.put("订单状态", Order.Status.fromCode(order.getStatus()).getDesc());
            data.put("创建时间", order.getCreateTime() != null ? 
                order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "未知");
            exportData.add(data);
        }

        return exportData;
    }

    @Override
    public List<Map<String, Object>> exportFinancialData(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (startDate != null) {
            wrapper.ge("create_time", startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le("create_time", endDate.atTime(23, 59, 59));
        }
        wrapper.eq("payment_status", 1); // 只导出已支付的订单

        List<Order> orders = orderRepository.selectList(wrapper);
        List<Map<String, Object>> exportData = new ArrayList<>();

        // 按日期统计
        Map<LocalDate, List<Order>> dateGroups = new HashMap<>();
        for (Order order : orders) {
            LocalDate date = order.getCreateTime().toLocalDate();
            dateGroups.computeIfAbsent(date, k -> new ArrayList<>()).add(order);
        }

        for (Map.Entry<LocalDate, List<Order>> entry : dateGroups.entrySet()) {
            LocalDate date = entry.getKey();
            List<Order> dayOrders = entry.getValue();

            Map<String, Object> data = new HashMap<>();
            data.put("日期", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            data.put("订单数量", dayOrders.size());

            BigDecimal dayRevenue = dayOrders.stream()
                    .filter(order -> order.getStatus() == Order.Status.COMPLETED.getCode())
                    .map(Order::getFinalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            data.put("营业额", dayRevenue);

            BigDecimal dayPaid = dayOrders.stream()
                    .map(Order::getFinalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            data.put("收款金额", dayPaid);

            exportData.add(data);
        }

        return exportData;
    }

    private String getPaymentMethodDesc(Integer paymentMethod) {
        if (paymentMethod == null) return "未知";
        switch (paymentMethod) {
            case 1: return "现金";
            case 2: return "微信支付";
            case 3: return "支付宝";
            case 4: return "银行卡";
            default: return "未知";
        }
    }

    @Override
    public Map<String, Object> getDailyReport(LocalDate date) {
        Map<String, Object> report = new HashMap<>();
        
        // 设置日期范围
        LocalDate startDate = date;
        LocalDate endDate = date;
        
        // 订单统计
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.ge("create_time", startDate.atStartOfDay());
        orderWrapper.le("create_time", endDate.atTime(23, 59, 59));
        
        List<Order> orders = orderRepository.selectList(orderWrapper);
        
        // 总订单数
        report.put("totalOrders", orders.size());
        
        // 已完成订单数
        long completedOrders = orders.stream()
                .filter(o -> o.getStatus().equals(Order.Status.COMPLETED.getCode()))
                .count();
        report.put("completedOrders", completedOrders);
        
        // 总营业额
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getStatus().equals(Order.Status.COMPLETED.getCode()))
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalRevenue", totalRevenue);
        
        // 平均订单金额
        BigDecimal avgOrderAmount = completedOrders > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(completedOrders), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;
        report.put("avgOrderAmount", avgOrderAmount);
        
        // 新注册用户数
        long newUsers = userRepository.selectCount(new QueryWrapper<User>()
                .ge("create_time", startDate.atStartOfDay())
                .le("create_time", endDate.atTime(23, 59, 59)));
        report.put("newUsers", newUsers);
        
        // 活跃门店数（有订单的门店）
        long activeShops = orders.stream()
                .map(Order::getShopId)
                .distinct()
                .count();
        report.put("activeShops", activeShops);
        
        report.put("date", date.toString());
        report.put("reportType", "日报");
        
        return report;
    }

    @Override
    public Map<String, Object> getWeeklyReport(LocalDate startDate) {
        Map<String, Object> report = new HashMap<>();
        
        // 周的结束日期
        LocalDate endDate = startDate.plusDays(6);
        
        // 订单统计
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.ge("create_time", startDate.atStartOfDay());
        orderWrapper.le("create_time", endDate.atTime(23, 59, 59));
        
        List<Order> orders = orderRepository.selectList(orderWrapper);
        
        // 总订单数
        report.put("totalOrders", orders.size());
        
        // 已完成订单数
        long completedOrders = orders.stream()
                .filter(o -> o.getStatus().equals(Order.Status.COMPLETED.getCode()))
                .count();
        report.put("completedOrders", completedOrders);
        
        // 总营业额
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getStatus().equals(Order.Status.COMPLETED.getCode()))
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalRevenue", totalRevenue);
        
        // 日均营业额
        BigDecimal dailyAvgRevenue = totalRevenue.divide(BigDecimal.valueOf(7), 2, BigDecimal.ROUND_HALF_UP);
        report.put("dailyAvgRevenue", dailyAvgRevenue);
        
        // 新注册用户数
        long newUsers = userRepository.selectCount(new QueryWrapper<User>()
                .ge("create_time", startDate.atStartOfDay())
                .le("create_time", endDate.atTime(23, 59, 59)));
        report.put("newUsers", newUsers);
        
        // 新增门店数
        long newShops = shopRepository.selectCount(new QueryWrapper<Shop>()
                .ge("create_time", startDate.atStartOfDay())
                .le("create_time", endDate.atTime(23, 59, 59)));
        report.put("newShops", newShops);
        
        // 每日订单趋势
        Map<String, Integer> dailyOrderTrend = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = startDate.plusDays(i);
            long dayOrders = orders.stream()
                    .filter(o -> o.getCreateTime().toLocalDate().equals(day))
                    .count();
            dailyOrderTrend.put(day.toString(), (int) dayOrders);
        }
        report.put("dailyOrderTrend", dailyOrderTrend);
        
        report.put("startDate", startDate.toString());
        report.put("endDate", endDate.toString());
        report.put("reportType", "周报");
        
        return report;
    }

    @Override
    public Map<String, Object> getMonthlyReport(int year, int month) {
        Map<String, Object> report = new HashMap<>();
        
        // 月的开始和结束日期
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        // 订单统计
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.ge("create_time", startDate.atStartOfDay());
        orderWrapper.le("create_time", endDate.atTime(23, 59, 59));
        
        List<Order> orders = orderRepository.selectList(orderWrapper);
        
        // 总订单数
        report.put("totalOrders", orders.size());
        
        // 已完成订单数
        long completedOrders = orders.stream()
                .filter(o -> o.getStatus().equals(Order.Status.COMPLETED.getCode()))
                .count();
        report.put("completedOrders", completedOrders);
        
        // 总营业额
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getStatus().equals(Order.Status.COMPLETED.getCode()))
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalRevenue", totalRevenue);
        
        // 日均营业额
        int daysInMonth = endDate.getDayOfMonth();
        BigDecimal dailyAvgRevenue = totalRevenue.divide(BigDecimal.valueOf(daysInMonth), 2, BigDecimal.ROUND_HALF_UP);
        report.put("dailyAvgRevenue", dailyAvgRevenue);
        
        // 新注册用户数
        long newUsers = userRepository.selectCount(new QueryWrapper<User>()
                .ge("create_time", startDate.atStartOfDay())
                .le("create_time", endDate.atTime(23, 59, 59)));
        report.put("newUsers", newUsers);
        
        // 新增门店数
        long newShops = shopRepository.selectCount(new QueryWrapper<Shop>()
                .ge("create_time", startDate.atStartOfDay())
                .le("create_time", endDate.atTime(23, 59, 59)));
        report.put("newShops", newShops);
        
        // 门店排行榜（按营业额）
        Map<Long, BigDecimal> shopRevenue = new HashMap<>();
        for (Order order : orders) {
            if (order.getStatus().equals(Order.Status.COMPLETED.getCode())) {
                shopRevenue.merge(order.getShopId(), order.getFinalAmount(), BigDecimal::add);
            }
        }
        
        // 获取前10名门店
        List<Map<String, Object>> topShops = shopRevenue.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> shopData = new HashMap<>();
                    Shop shop = shopRepository.selectById(entry.getKey());
                    shopData.put("shopId", entry.getKey());
                    shopData.put("shopName", shop != null ? shop.getShopName() : "未知门店");
                    shopData.put("revenue", entry.getValue());
                    return shopData;
                })
                .collect(java.util.stream.Collectors.toList());
        
        report.put("topShops", topShops);
        report.put("year", year);
        report.put("month", month);
        report.put("reportType", "月报");
        
        return report;
    }

    @Override
    public Page<Map<String, Object>> getReviewList(Page<?> page, String keyword) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        
        // 添加关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like("comment", keyword);
        }
        
        wrapper.orderByDesc("create_time");
        
        Page<Review> reviewPage = new Page<>(page.getCurrent(), page.getSize());
        reviewRepository.selectPage(reviewPage, wrapper);
        
        // 转换为包含关联信息的Map
        Page<Map<String, Object>> resultPage = new Page<>(page.getCurrent(), page.getSize(), reviewPage.getTotal());
        List<Map<String, Object>> reviewList = new ArrayList<>();
        
        for (Review review : reviewPage.getRecords()) {
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("id", review.getId());
            reviewMap.put("orderId", review.getOrderId());
            reviewMap.put("userId", review.getUserId());
            reviewMap.put("shopId", review.getShopId());
            reviewMap.put("technicianRating", review.getTechnicianRating());
            reviewMap.put("serviceRating", review.getServiceRating());
            reviewMap.put("priceRating", review.getPriceRating());
            reviewMap.put("environmentRating", review.getEnvironmentRating());
            reviewMap.put("overallRating", review.getOverallRating());
            reviewMap.put("rating", review.getOverallRating()); // 兼容前端字段
            reviewMap.put("comment", review.getComment());
            reviewMap.put("content", review.getComment()); // 兼容前端字段
            reviewMap.put("reply", review.getReply());
            reviewMap.put("status", review.getStatus());
            reviewMap.put("createTime", review.getCreateTime() != null ? 
                review.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
            
            // 获取用户信息
            User user = userRepository.selectById(review.getUserId());
            if (user != null) {
                reviewMap.put("userName", user.getRealName() != null ? user.getRealName() : user.getUsername());
            } else {
                reviewMap.put("userName", "未知用户");
            }
            
            // 获取门店信息
            Shop shop = shopRepository.selectById(review.getShopId());
            if (shop != null) {
                reviewMap.put("shopName", shop.getShopName());
            } else {
                reviewMap.put("shopName", "未知门店");
            }
            
            // 获取订单号
            Order order = orderRepository.selectById(review.getOrderId());
            if (order != null) {
                reviewMap.put("orderNo", order.getOrderNo());
            } else {
                reviewMap.put("orderNo", "未知订单");
            }
            
            reviewList.add(reviewMap);
        }
        
        resultPage.setRecords(reviewList);
        return resultPage;
    }
}