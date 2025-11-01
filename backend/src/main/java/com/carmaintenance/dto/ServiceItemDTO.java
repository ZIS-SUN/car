package com.carmaintenance.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * 服务项目DTO
 */
@Data
public class ServiceItemDTO {

    private Long id; // 更新时需要

    @NotBlank(message = "项目名称不能为空")
    private String itemName;

    @NotBlank(message = "项目分类不能为空")
    private String category;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    @NotNull(message = "标准工时不能为空")
    @Min(value = 1, message = "标准工时至少为1分钟")
    private Integer duration;

    private String description;

    private String iconUrl;

    private Integer status = 1; // 默认上架
}