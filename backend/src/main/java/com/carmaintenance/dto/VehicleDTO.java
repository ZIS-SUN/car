package com.carmaintenance.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 车辆DTO
 */
@Data
public class VehicleDTO {

    private Long id; // 更新时需要

    @NotBlank(message = "车辆品牌不能为空")
    private String brand;

    @NotBlank(message = "车型不能为空")
    private String model;

    @NotBlank(message = "车牌号不能为空")
    @Pattern(regexp = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$",
             message = "车牌号格式不正确")
    private String licensePlate;

    private String color;

    private Integer year;

    private String displacement;

    private String transmission;
}