package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户会员信息实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_members")
public class UserMember {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("level_id")
    private Integer levelId;

    @TableField("total_experience")
    private Integer totalExperience;

    @TableField("available_experience")
    private Integer availableExperience;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}