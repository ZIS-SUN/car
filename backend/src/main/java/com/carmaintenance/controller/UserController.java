package com.carmaintenance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carmaintenance.dto.Result;
import com.carmaintenance.entity.User;
import com.carmaintenance.security.UserPrincipal;
import com.carmaintenance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<User> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userService.getById(userPrincipal.getUserId());
        // 清除密码信息
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public Result<String> updateProfile(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                       @RequestBody User user) {
        try {
            user.setId(userPrincipal.getUserId());
            // 不允许更新用户名、密码、用户类型等关键字段
            user.setUsername(null);
            user.setPassword(null);
            user.setUserType(null);
            user.setStatus(null);

            if (userService.updateById(user)) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询用户列表 - 仅管理员
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) String keyword) {

        Page<User> page = new Page<>(current, size);
        Page<User> userPage = userService.getUserPage(page, userType, keyword);

        // 清除密码信息
        userPage.getRecords().forEach(user -> user.setPassword(null));

        return Result.success(userPage);
    }

    /**
     * 更新用户状态 - 仅管理员
     */
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateUserStatus(@PathVariable Long userId,
                                          @RequestParam Integer status) {
        try {
            if (userService.updateUserStatus(userId, status)) {
                return Result.success("状态更新成功");
            } else {
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}