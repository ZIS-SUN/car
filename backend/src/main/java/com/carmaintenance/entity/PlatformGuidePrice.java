package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 平台指导价实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("platform_guide_prices")
public class PlatformGuidePrice {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("service_name")
    private String serviceName;

    @TableField("category")
    private String category;

    @TableField("guide_price")
    private BigDecimal guidePrice;

    @TableField("min_price")
    private BigDecimal minPrice;

    @TableField("max_price")
    private BigDecimal maxPrice;

    @TableField("description")
    private String description;

    @TableField("status")
    private Integer status; // 0-禁用, 1-启用

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}






