package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 套餐项目关联实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("package_items")
public class PackageItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("package_id")
    private Long packageId;

    @TableField("item_id")
    private Long itemId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}