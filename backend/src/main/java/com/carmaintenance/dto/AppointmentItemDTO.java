package com.carmaintenance.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * 预约项目DTO
 */
@Data
public class AppointmentItemDTO {

    private Long itemId;        // 服务项目ID

    private Long packageId;     // 套餐ID

    @NotNull(message = "项目名称不能为空")
    private String itemName;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    @Min(value = 1, message = "数量至少为1")
    private Integer quantity = 1;
}