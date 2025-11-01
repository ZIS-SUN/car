package com.carmaintenance.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;

/**
 * 服务套餐DTO
 */
@Data
public class ServicePackageDTO {

    private Long id; // 更新时需要

    @NotBlank(message = "套餐名称不能为空")
    private String packageName;

    @NotNull(message = "原价不能为空")
    private BigDecimal originalPrice;

    @NotNull(message = "套餐价格不能为空")
    private BigDecimal packagePrice;

    private String description;

    private String coverUrl;

    private Integer status = 1; // 默认上架

    private List<Long> itemIds; // 包含的项目ID列表
}