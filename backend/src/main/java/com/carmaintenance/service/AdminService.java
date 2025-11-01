package com.carmaintenance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carmaintenance.entity.Order;
import com.carmaintenance.entity.Shop;
import com.carmaintenance.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 管理员Service接口
 */
public interface AdminService {

    // 用户管理
    /**
     * 获取用户列表（分页）
     */
    Page<User> getUserList(Page<User> page, Integer userType, String keyword, String status);

    /**
     * 更新用户状态
     */
    boolean updateUserStatus(Long userId, Integer status);

    /**
     * 删除用户
     */
    boolean deleteUser(Long userId);

    /**
     * 获取用户统计信息
     */
    Map<String, Object> getUserStats();

    // 门店管理
    /**
     * 获取门店列表（分页）
     */
    Page<Shop> getShopList(Page<Shop> page, String city, String keyword, Integer status);

    /**
     * 审核门店
     */
    boolean approveShop(Long shopId, Integer status);

    /**
     * 获取门店统计信息
     */
    Map<String, Object> getShopStats();

    /**
     * 创建门店
     */
    boolean createShop(Shop shop);

    /**
     * 更新门店信息
     */
    boolean updateShop(Shop shop);

    /**
     * 删除门店
     */
    boolean deleteShop(Long shopId);

    // 订单管理
    /**
     * 获取订单列表（分页）
     */
    Page<Order> getOrderList(Page<Order> page, Long userId, Long shopId, Integer status, LocalDate startDate, LocalDate endDate);

    /**
     * 获取订单统计信息
     */
    Map<String, Object> getOrderStats(LocalDate startDate, LocalDate endDate);

    // 评价管理
    /**
     * 获取评价列表（分页）
     */
    Page<Map<String, Object>> getReviewList(Page<?> page, String keyword);

    // 系统配置
    /**
     * 获取系统配置
     */
    Map<String, String> getSystemConfigs();

    /**
     * 更新系统配置
     */
    boolean updateSystemConfig(String configKey, String configValue);

    /**
     * 获取系统运行统计
     */
    Map<String, Object> getSystemStats();

    // 数据导出
    /**
     * 导出用户数据
     */
    List<Map<String, Object>> exportUsers(Integer userType);

    /**
     * 导出订单数据
     */
    List<Map<String, Object>> exportOrders(LocalDate startDate, LocalDate endDate);

    /**
     * 导出财务数据
     */
    List<Map<String, Object>> exportFinancialData(LocalDate startDate, LocalDate endDate);

    // 数据统计报表
    /**
     * 获取日报数据
     */
    Map<String, Object> getDailyReport(LocalDate date);

    /**
     * 获取周报数据
     */
    Map<String, Object> getWeeklyReport(LocalDate startDate);

    /**
     * 获取月报数据
     */
    Map<String, Object> getMonthlyReport(int year, int month);
}