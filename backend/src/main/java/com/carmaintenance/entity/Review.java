package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评价实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("reviews")
public class Review {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("user_id")
    private Long userId;

    @TableField("shop_id")
    private Long shopId;

    @TableField("technician_rating")
    private Integer technicianRating; // 技师评分(1-5)

    @TableField("service_rating")
    private Integer serviceRating; // 服务评分(1-5)

    @TableField("price_rating")
    private Integer priceRating; // 价格评分(1-5)

    @TableField("environment_rating")
    private Integer environmentRating; // 环境评分(1-5)

    @TableField("overall_rating")
    private BigDecimal overallRating; // 综合评分

    @TableField("comment")
    private String comment; // 评价内容

    @TableField("reply")
    private String reply; // 商家回复

    @TableField(exist = false)
    private Integer isAnonymous; // 是否匿名: 0-否, 1-是 (数据库中不存在此字段)

    @TableField("status")
    private Integer status; // 状态: 0-隐藏, 1-显示

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted; // 逻辑删除
}