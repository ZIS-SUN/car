package com.carmaintenance.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

/**
 * 门店注册DTO
 */
@Data
public class ShopRegisterDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "门店名称不能为空")
    private String shopName;

    @NotBlank(message = "详细地址不能为空")
    private String address;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "区县不能为空")
    private String district;

    private Double longitude;

    private Double latitude;

    @NotBlank(message = "营业时间不能为空")
    private String businessHours;

    @Min(value = 1, message = "工位数量至少为1")
    @Max(value = 20, message = "工位数量不能超过20")
    private Integer shopBays = 1;

    private String description;
}