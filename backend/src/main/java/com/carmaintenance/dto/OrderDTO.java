package com.carmaintenance.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * 订单DTO
 */
@Data
public class OrderDTO {

    @NotNull(message = "预约ID不能为空")
    private Long appointmentId;

    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod; // 1-现金, 2-微信, 3-支付宝, 4-银行卡

    private Long technicianId;

    private String remark;
}