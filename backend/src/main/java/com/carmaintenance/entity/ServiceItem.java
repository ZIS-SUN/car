package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务项目实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("service_items")
public class ServiceItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("shop_id")
    private Long shopId;

    @TableField("name")
    private String itemName;

    @TableField("category")
    private String category;

    @TableField("price")
    private BigDecimal price;

    @TableField("duration_minutes")
    private Integer duration; // 标准工时(分钟)

    @TableField("description")
    private String description;

    @TableField("image_url")
    private String iconUrl;

    @TableField("status")
    private Integer status; // 0-下架, 1-上架

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}