package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门店实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("shops")
public class Shop {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("shop_name")
    private String shopName;

    @TableField("description")
    private String description;

    @TableField("address")
    private String address;

    @TableField(exist = false)
    private String province;

    @TableField("city")
    private String city;

    @TableField(exist = false)
    private String district;

    @TableField(exist = false)
    private BigDecimal longitude;

    @TableField(exist = false)
    private BigDecimal latitude;

    @TableField("phone")
    private String phone;

    @TableField(exist = false)
    private String email;

    @TableField("business_hours")
    private String businessHours;

    @TableField(exist = false)
    private Integer shopBays;

    @TableField("images")
    private String images;

    @TableField(exist = false)
    private String logoUrl;

    @TableField("status")
    private Integer status; // 0-待审核, 1-正常, 2-已禁用

    @TableField(exist = false)
    private BigDecimal rating;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}