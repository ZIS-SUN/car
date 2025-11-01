package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约项目实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("appointment_items")
public class AppointmentItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("appointment_id")
    private Long appointmentId;

    @TableField("item_id")
    private Long itemId;

    @TableField("package_id")
    private Long packageId;

    @TableField("item_name")
    private String itemName;

    @TableField("price")
    private BigDecimal price;

    @TableField("quantity")
    private Integer quantity;

    @TableField("subtotal")
    private BigDecimal subtotal;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}