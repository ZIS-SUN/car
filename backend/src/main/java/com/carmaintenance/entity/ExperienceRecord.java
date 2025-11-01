package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 经验记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("experience_records")
public class ExperienceRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("order_id")
    private Long orderId;

    @TableField("experience_change")
    private Integer experienceChange;

    @TableField("experience_type")
    private Integer experienceType; // 1-消费获得, 2-投诉扣减, 3-手动调整

    @TableField("reason")
    private String reason;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public enum ExperienceType {
        CONSUME(1, "消费获得"),
        COMPLAINT(2, "投诉扣减"),
        MANUAL(3, "手动调整");

        private final Integer code;
        private final String desc;

        ExperienceType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static ExperienceType fromCode(Integer code) {
            for (ExperienceType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown experience type code: " + code);
        }
    }
}