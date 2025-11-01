package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务套餐实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("service_packages")
public class ServicePackage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("shop_id")
    private Long shopId;

    @TableField("package_name")
    private String packageName;

    @TableField("description")
    private String description;

    @TableField("package_price")
    private BigDecimal packagePrice;

    @TableField("original_price")
    private BigDecimal originalPrice;

    @TableField("status")
    private Integer status; // 0-下架, 1-上架

    @TableField("image_url")
    private String coverUrl;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}