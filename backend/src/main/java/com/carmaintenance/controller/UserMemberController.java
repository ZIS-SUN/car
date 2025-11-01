package com.carmaintenance.controller;

import com.carmaintenance.dto.Result;
import com.carmaintenance.security.UserPrincipal;
import com.carmaintenance.service.UserMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 用户会员控制器
 */
@RestController
@RequestMapping("/user-member")
public class UserMemberController {

    @Autowired
    private UserMemberService userMemberService;

    /**
     * 初始化用户会员信息
     */
    @PostMapping("/init")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<String> initUserMember(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        String message = userMemberService.initUserMember(userPrincipal.getUserId());
        return Result.success(message);
    }

    /**
     * 获取用户会员信息
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Map<String, Object>> getMyMemberInfo(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> memberInfo = userMemberService.getUserMemberInfo(userPrincipal.getUserId());
        return Result.success(memberInfo);
    }

    /**
     * 获取用户会员信息（管理员或指定用户）
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public Result<Map<String, Object>> getUserMemberInfo(@PathVariable Long userId,
                                                       @AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 检查权限：管理员可以查看所有用户，用户只能查看自己
        if (!userPrincipal.getUserType().equals(3) && !userPrincipal.getUserId().equals(userId)) {
            return Result.error("无权查看该用户信息");
        }

        Map<String, Object> memberInfo = userMemberService.getUserMemberInfo(userId);
        return Result.success(memberInfo);
    }

    /**
     * 增加用户经验值（管理员）
     */
    @PostMapping("/{userId}/add-experience")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> addExperience(@PathVariable Long userId,
                                      @RequestParam Integer experience,
                                      @RequestParam String reason) {
        String message = userMemberService.addExperience(userId, experience, 3, null, reason);
        return Result.success(message);
    }

    /**
     * 扣减用户经验值（管理员）
     */
    @PostMapping("/{userId}/deduct-experience")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deductExperience(@PathVariable Long userId,
                                         @RequestParam Integer experience,
                                         @RequestParam String reason) {
        String message = userMemberService.deductExperience(userId, experience, reason);
        return Result.success(message);
    }

    /**
     * 手动调整经验值（管理员）
     */
    @PostMapping("/{userId}/adjust-experience")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> adjustExperience(@PathVariable Long userId,
                                         @RequestParam Integer experience,
                                         @RequestParam String reason) {
        String message = userMemberService.adjustExperience(userId, experience, reason);
        return Result.success(message);
    }

    /**
     * 更新用户等级（管理员）
     */
    @PutMapping("/{userId}/level")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateUserLevel(@PathVariable Long userId,
                                         @RequestParam Integer levelId) {
        if (userMemberService.updateUserLevel(userId, levelId)) {
            return Result.success("用户等级更新成功");
        } else {
            return Result.error("用户等级更新失败");
        }
    }

    /**
     * 计算用户折扣
     */
    @GetMapping("/{userId}/discount")
    public Result<BigDecimal> calculateUserDiscount(@PathVariable Long userId,
                                                   @RequestParam BigDecimal originalAmount) {
        BigDecimal discount = userMemberService.calculateUserDiscount(userId, originalAmount);
        return Result.success(discount);
    }

    /**
     * 获取用户经验记录
     */
    @GetMapping("/{userId}/experience-records")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public Result<Map<String, Object>> getUserExperienceRecords(@PathVariable Long userId,
                                                               @RequestParam(defaultValue = "1") Integer page,
                                                               @RequestParam(defaultValue = "10") Integer size,
                                                               @AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 检查权限：管理员可以查看所有用户，用户只能查看自己
        if (!userPrincipal.getUserType().equals(3) && !userPrincipal.getUserId().equals(userId)) {
            return Result.error("无权查看该用户记录");
        }

        Map<String, Object> records = userMemberService.getUserExperienceRecords(userId, page, size);
        return Result.success(records);
    }

    /**
     * 检查用户是否可以升级
     */
    @GetMapping("/{userId}/check-upgrade")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public Result<Map<String, Object>> checkUserUpgrade(@PathVariable Long userId,
                                                       @AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 检查权限：管理员可以查看所有用户，用户只能查看自己
        if (!userPrincipal.getUserType().equals(3) && !userPrincipal.getUserId().equals(userId)) {
            return Result.error("无权查看该用户信息");
        }

        Map<String, Object> upgradeInfo = userMemberService.checkUserUpgrade(userId);
        return Result.success(upgradeInfo);
    }
}