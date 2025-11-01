package com.carmaintenance.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户登录DTO
 */
@Data
public class UserLoginDTO {

    @NotBlank(message = "用户名或邮箱不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}