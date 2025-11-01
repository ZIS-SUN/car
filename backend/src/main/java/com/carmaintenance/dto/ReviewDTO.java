package com.carmaintenance.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 评价DTO
 */
@Data
public class ReviewDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Min(value = 1, message = "技师评分不能低于1分")
    @Max(value = 5, message = "技师评分不能高于5分")
    private Integer technicianRating;

    @Min(value = 1, message = "服务评分不能低于1分")
    @Max(value = 5, message = "服务评分不能高于5分")
    private Integer serviceRating;

    @Min(value = 1, message = "价格评分不能低于1分")
    @Max(value = 5, message = "价格评分不能高于5分")
    private Integer priceRating;

    @Min(value = 1, message = "环境评分不能低于1分")
    @Max(value = 5, message = "环境评分不能高于5分")
    private Integer environmentRating;

    private String comment;

    private Integer isAnonymous = 0; // 默认不匿名
}