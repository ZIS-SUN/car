package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 车辆实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("vehicles")
public class Vehicle {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("brand")
    private String brand;

    @TableField("model")
    private String model;

    @TableField("license_plate")
    private String licensePlate;

    @TableField("color")
    private String color;

    @TableField("year")
    private Integer year;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}