package com.carmaintenance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carmaintenance.dto.Result;
import com.carmaintenance.entity.*;
import com.carmaintenance.repository.ReviewRepository;
import com.carmaintenance.service.AdminService;
import com.carmaintenance.utils.ExcelExportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ReviewRepository reviewRepository;

    // ==================== 用户管理 ====================

    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public Result<Page<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {

        Page<User> page = new Page<>(current, size);
        Page<User> userPage = adminService.getUserList(page, userType, keyword, status);

        // 清除密码信息
        userPage.getRecords().forEach(user -> user.setPassword(null));

        return Result.success(userPage);
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/users/{userId}/status")
    public Result<String> updateUserStatus(@PathVariable Long userId,
                                          @RequestParam Integer status) {
        if (adminService.updateUserStatus(userId, status)) {
            String statusText = status == 1 ? "启用" : "禁用";
            return Result.success("用户已" + statusText);
        } else {
            return Result.error("操作失败");
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/users/stats")
    public Result<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = adminService.getUserStats();
        return Result.success(stats);
    }

    // ==================== 门店管理 ====================

    /**
     * 获取门店列表
     */
    @GetMapping("/shops")
    public Result<Page<Shop>> getShopList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        Page<Shop> page = new Page<>(current, size);
        Page<Shop> shopPage = adminService.getShopList(page, city, keyword, status);

        return Result.success(shopPage);
    }

    /**
     * 审核门店(兼容带status参数的请求)
     */
    @PutMapping("/shops/{shopId}/status")
    public Result<String> updateShopStatus(@PathVariable Long shopId,
                                          @RequestParam Integer status) {
        if (adminService.approveShop(shopId, status)) {
            String statusText = status == 1 ? "审核通过" : "审核拒绝";
            return Result.success("门店" + statusText);
        } else {
            return Result.error("操作失败");
        }
    }

    /**
     * 获取门店统计信息
     */
    @GetMapping("/shops/stats")
    public Result<Map<String, Object>> getShopStats() {
        Map<String, Object> stats = adminService.getShopStats();
        return Result.success(stats);
    }

    /**
     * 创建门店
     */
    @PostMapping("/shops")
    public Result<String> createShop(@RequestBody Shop shop) {
        if (adminService.createShop(shop)) {
            return Result.success("门店创建成功");
        } else {
            return Result.error("门店创建失败");
        }
    }

    /**
     * 更新门店信息
     */
    @PutMapping("/shops/{shopId}")
    public Result<String> updateShop(@PathVariable Long shopId, @RequestBody Shop shop) {
        shop.setId(shopId);
        if (adminService.updateShop(shop)) {
            return Result.success("门店更新成功");
        } else {
            return Result.error("门店更新失败");
        }
    }

    /**
     * 删除门店
     */
    @DeleteMapping("/shops/{shopId}")
    public Result<String> deleteShop(@PathVariable Long shopId) {
        if (adminService.deleteShop(shopId)) {
            return Result.success("门店删除成功");
        } else {
            return Result.error("门店删除失败");
        }
    }

    // ==================== 订单管理 ====================

    /**
     * 获取订单列表
     */
    @GetMapping("/orders")
    public Result<Page<Order>> getOrderList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        Page<Order> page = new Page<>(current, size);
        Page<Order> orderPage = adminService.getOrderList(page, userId, shopId, status, startDate, endDate);

        return Result.success(orderPage);
    }

    /**
     * 获取订单统计信息
     */
    @GetMapping("/orders/stats")
    public Result<Map<String, Object>> getOrderStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Map<String, Object> stats = adminService.getOrderStats(startDate, endDate);
        return Result.success(stats);
    }

    // ==================== 系统配置 ====================

    /**
     * 获取系统配置
     */
    @GetMapping("/configs")
    public Result<Map<String, String>> getSystemConfigs() {
        Map<String, String> configs = adminService.getSystemConfigs();
        return Result.success(configs);
    }

    /**
     * 更新系统配置
     */
    @PutMapping("/configs")
    public Result<String> updateSystemConfig(@RequestParam String configKey,
                                            @RequestParam String configValue) {
        if (adminService.updateSystemConfig(configKey, configValue)) {
            return Result.success("配置更新成功");
        } else {
            return Result.error("配置更新失败");
        }
    }

    /**
     * 获取系统运行统计
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getSystemStats() {
        Map<String, Object> stats = adminService.getSystemStats();
        return Result.success(stats);
    }

    // ==================== 数据导出 ====================

    /**
     * 导出用户数据为Excel
     */
    @GetMapping("/export/users")
    public void exportUsers(@RequestParam(required = false) Integer userType,
                          HttpServletResponse response) throws IOException {
        List<Map<String, Object>> data = adminService.exportUsers(userType);
        List<String> headers = Arrays.asList("用户ID", "用户名", "真实姓名", "邮箱", "手机号", "用户类型", "状态", "注册时间");
        String fileName = "用户数据_" + System.currentTimeMillis();
        ExcelExportUtil.exportToExcel(fileName, headers, data, response);
    }

    /**
     * 导出订单数据为Excel
     */
    @GetMapping("/export/orders")
    public void exportOrders(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                            HttpServletResponse response) throws IOException {
        List<Map<String, Object>> data = adminService.exportOrders(startDate, endDate);
        List<String> headers = Arrays.asList("订单ID", "订单编号", "用户ID", "门店ID", "总金额", 
                                            "折扣金额", "实付金额", "支付方式", "支付状态", "订单状态", "创建时间");
        String fileName = "订单数据_" + System.currentTimeMillis();
        ExcelExportUtil.exportToExcel(fileName, headers, data, response);
    }

    /**
     * 导出财务数据为Excel
     */
    @GetMapping("/export/financial")
    public void exportFinancialData(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                   HttpServletResponse response) throws IOException {
        List<Map<String, Object>> data = adminService.exportFinancialData(startDate, endDate);
        List<String> headers = Arrays.asList("日期", "订单数量", "营业额", "收款金额");
        String fileName = "财务数据_" + System.currentTimeMillis();
        ExcelExportUtil.exportToExcel(fileName, headers, data, response);
    }

    /**
     * 获取统计数据(用于首页仪表板)
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();

        // 获取基础统计数据
        Map<String, Object> userStats = adminService.getUserStats();
        Map<String, Object> shopStats = adminService.getShopStats();
        Map<String, Object> orderStats = adminService.getOrderStats(null, null);

        stats.put("totalUsers", userStats.get("totalUsers"));
        stats.put("totalShops", shopStats.get("totalShops"));
        stats.put("totalOrders", orderStats.get("totalOrders"));
        stats.put("totalRevenue", orderStats.get("totalRevenue"));

        return Result.success(stats);
    }

    /**
     * 获取最近订单列表
     */
    @GetMapping("/orders/recent")
    public Result<Page<Order>> getRecentOrders(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Order> page = new Page<>(current, size);
        Page<Order> orderPage = adminService.getOrderList(page, null, null, null, null, null);
        return Result.success(orderPage);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    public Result<String> deleteUser(@PathVariable Long userId) {
        if (adminService.deleteUser(userId)) {
            return Result.success("用户删除成功");
        } else {
            return Result.error("用户删除失败");
        }
    }

    /**
     * 审核通过门店
     */
    @PutMapping("/shops/{shopId}/approve")
    public Result<String> approveShop(@PathVariable Long shopId) {
        if (adminService.approveShop(shopId, 1)) {
            return Result.success("门店审核通过");
        } else {
            return Result.error("操作失败");
        }
    }

    /**
     * 拒绝门店
     */
    @PutMapping("/shops/{shopId}/reject")
    public Result<String> rejectShop(@PathVariable Long shopId,
                                    @RequestBody Map<String, String> request) {
        // String reason = request.get("reason"); // 后续可用于记录拒绝原因
        // 状态设置为已拒绝
        if (adminService.approveShop(shopId, 3)) {
            return Result.success("门店已拒绝");
        } else {
            return Result.error("操作失败");
        }
    }

    /**
     * 暂停门店
     */
    @PutMapping("/shops/{shopId}/suspend")
    public Result<String> suspendShop(@PathVariable Long shopId) {
        // 状态设置为已暂停
        if (adminService.approveShop(shopId, 2)) {
            return Result.success("门店已暂停");
        } else {
            return Result.error("操作失败");
        }
    }

    /**
     * 激活门店
     */
    @PutMapping("/shops/{shopId}/activate")
    public Result<String> activateShop(@PathVariable Long shopId) {
        if (adminService.approveShop(shopId, 1)) {
            return Result.success("门店已激活");
        } else {
            return Result.error("操作失败");
        }
    }

    // ==================== 评价管理 ====================

    /**
     * 获取评价列表
     */
    @GetMapping("/reviews")
    public Result<Page<Map<String, Object>>> getReviews(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<?> page = new Page<>(current, size);
        Page<Map<String, Object>> reviewPage = adminService.getReviewList(page, keyword);
        return Result.success(reviewPage);
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/reviews/{reviewId}")
    public Result<String> deleteReview(@PathVariable Long reviewId) {
        Review review = reviewRepository.selectById(reviewId);
        if (review == null) {
            return Result.error("评价不存在");
        }
        
        boolean result = reviewRepository.deleteById(reviewId) > 0;
        if (result) {
            return Result.success("评价删除成功");
        } else {
            return Result.error("评价删除失败");
        }
    }

    // ==================== 数据统计报表 ====================

    /**
     * 获取日报数据
     */
    @GetMapping("/reports/daily")
    public Result<Map<String, Object>> getDailyReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        LocalDate reportDate = date != null ? date : LocalDate.now();
        Map<String, Object> report = adminService.getDailyReport(reportDate);
        return Result.success(report);
    }

    /**
     * 获取周报数据
     */
    @GetMapping("/reports/weekly")
    public Result<Map<String, Object>> getWeeklyReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate) {
        LocalDate reportStart = startDate != null ? startDate : LocalDate.now().minusDays(6);
        Map<String, Object> report = adminService.getWeeklyReport(reportStart);
        return Result.success(report);
    }

    /**
     * 获取月报数据
     */
    @GetMapping("/reports/monthly")
    public Result<Map<String, Object>> getMonthlyReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        int reportYear = year != null ? year : LocalDate.now().getYear();
        int reportMonth = month != null ? month : LocalDate.now().getMonthValue();
        Map<String, Object> report = adminService.getMonthlyReport(reportYear, reportMonth);
        return Result.success(report);
    }
}