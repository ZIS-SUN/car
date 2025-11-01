package com.carmaintenance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carmaintenance.dto.AppointmentDTO;
import com.carmaintenance.dto.Result;
import com.carmaintenance.entity.Appointment;
import com.carmaintenance.entity.Shop;
import com.carmaintenance.repository.ShopRepository;
import com.carmaintenance.security.UserPrincipal;
import com.carmaintenance.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 预约控制器
 */
@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ShopRepository shopRepository;

    /**
     * 创建预约
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<String> createAppointment(@Valid @RequestBody AppointmentDTO appointmentDTO,
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        String message = appointmentService.createAppointment(appointmentDTO, userPrincipal.getUserId());
        return Result.success(message);
    }

    /**
     * 取消预约
     */
    @PutMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<String> cancelAppointment(@PathVariable Long appointmentId,
                                           @RequestParam(required = false) String reason,
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (appointmentService.cancelAppointment(appointmentId, userPrincipal.getUserId(), reason)) {
            return Result.success("预约取消成功");
        } else {
            return Result.error("预约取消失败");
        }
    }

    /**
     * 修改预约
     */
    @PutMapping("/{appointmentId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<String> updateAppointment(@PathVariable Long appointmentId,
                                           @Valid @RequestBody AppointmentDTO appointmentDTO,
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (appointmentService.updateAppointment(appointmentId, appointmentDTO, userPrincipal.getUserId())) {
            return Result.success("预约修改成功");
        } else {
            return Result.error("预约修改失败");
        }
    }

    /**
     * 获取用户预约列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Page<Appointment>> getMyAppointments(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Page<Appointment> page = new Page<>(current, size);
        Page<Appointment> appointmentPage = appointmentService.getUserAppointments(page, userPrincipal.getUserId(), status);

        return Result.success(appointmentPage);
    }

    /**
     * 获取门店预约列表
     */
    @GetMapping("/shop")
    @PreAuthorize("hasRole('SHOP')")
    public Result<Page<Appointment>> getShopAppointments(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // 根据用户ID获取门店ID
        Shop shop = shopRepository.findByUserId(userPrincipal.getUserId());
        if (shop == null) {
            return Result.error("门店信息不存在");
        }
        Long shopId = shop.getId();

        Page<Appointment> page = new Page<>(current, size);
        Page<Appointment> appointmentPage = appointmentService.getShopAppointments(page, shopId, status, date);

        return Result.success(appointmentPage);
    }

    /**
     * 获取预约详情
     */
    @GetMapping("/{appointmentId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SHOP')")
    public Result<Map<String, Object>> getAppointmentDetail(@PathVariable Long appointmentId,
                                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> detail = appointmentService.getAppointmentDetail(appointmentId, userPrincipal.getUserId());
        return Result.success(detail);
    }

    /**
     * 检查时间段是否可用
     */
    @GetMapping("/check-slot")
    public Result<Boolean> checkTimeSlotAvailable(@RequestParam Long shopId,
                                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                                  @RequestParam String timeSlot) {
        boolean available = appointmentService.checkTimeSlotAvailable(shopId, date, timeSlot);
        return Result.success(available);
    }

    /**
     * 获取可用时间段
     */
    @GetMapping("/available-slots")
    public Result<List<String>> getAvailableTimeSlots(@RequestParam Long shopId,
                                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<String> slots = appointmentService.getAvailableTimeSlots(shopId, date);
        return Result.success(slots);
    }

    /**
     * 更新预约状态（门店用户）
     */
    @PutMapping("/{appointmentId}/status")
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> updateAppointmentStatus(@PathVariable Long appointmentId,
                                                 @RequestParam Integer status,
                                                 @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (appointmentService.updateAppointmentStatus(appointmentId, status)) {
            return Result.success("状态更新成功");
        } else {
            return Result.error("状态更新失败");
        }
    }

    /**
     * 分配工位（门店用户）
     */
    @PutMapping("/{appointmentId}/bay")
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> assignBay(@PathVariable Long appointmentId,
                                   @RequestParam Integer bayNumber,
                                   @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (appointmentService.assignBay(appointmentId, bayNumber)) {
            return Result.success("工位分配成功");
        } else {
            return Result.error("工位分配失败");
        }
    }

    /**
     * 获取预约统计信息（门店用户）
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SHOP')")
    public Result<Map<String, Object>> getAppointmentStats(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 根据用户ID获取门店ID
        Shop shop = shopRepository.findByUserId(userPrincipal.getUserId());
        if (shop == null) {
            return Result.error("门店信息不存在");
        }
        Long shopId = shop.getId();
        Map<String, Object> stats = appointmentService.getAppointmentStats(shopId);
        return Result.success(stats);
    }
}