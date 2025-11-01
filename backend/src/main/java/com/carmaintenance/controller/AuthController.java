package com.carmaintenance.controller;

import com.carmaintenance.dto.Result;
import com.carmaintenance.dto.UserLoginDTO;
import com.carmaintenance.dto.UserRegisterDTO;
import com.carmaintenance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        String message = userService.register(registerDTO);
        return Result.success(message);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        String token = userService.login(loginDTO);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);

        return Result.success("登录成功", data);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        // 由于使用JWT，服务端无需做特殊处理，客户端删除token即可
        return Result.success("退出成功");
    }
}