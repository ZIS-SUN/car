package com.carmaintenance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.dto.UserLoginDTO;
import com.carmaintenance.dto.UserRegisterDTO;
import com.carmaintenance.entity.User;

/**
 * 用户Service接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    String register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     */
    String login(UserLoginDTO loginDTO);

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    User findByEmail(String email);

    /**
     * 分页查询用户列表
     */
    Page<User> getUserPage(Page<User> page, Integer userType, String keyword);

    /**
     * 更新用户状态
     */
    boolean updateUserStatus(Long userId, Integer status);

    /**
     * 重置密码
     */
    boolean resetPassword(String email, String newPassword);
}