package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员等级实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("member_levels")
public class MemberLevel {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("level_name")
    private String levelName;

    @TableField("min_experience")
    private Integer minExperience;

    @TableField("discount_rate")
    private BigDecimal discountRate;

    @TableField("benefits")
    private String benefits;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}