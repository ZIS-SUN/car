package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("appointments")
public class Appointment {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("appointment_no")
    private String appointmentNo;

    @TableField("user_id")
    private Long userId;

    @TableField("shop_id")
    private Long shopId;

    @TableField("vehicle_id")
    private Long vehicleId;

    @TableField("appointment_date")
    private LocalDate appointmentDate;

    @TableField("time_slot")
    private String timeSlot;

    @TableField("bay_number")
    private Integer bayNumber;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("status")
    private Integer status; // 1-待服务, 2-进行中, 3-已完成, 4-已取消, 5-已违约

    @TableField("cancel_reason")
    private String cancelReason;

    @TableField("cancel_time")
    private LocalDateTime cancelTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public enum Status {
        PENDING(1, "待服务"),
        IN_PROGRESS(2, "进行中"),
        COMPLETED(3, "已完成"),
        CANCELLED(4, "已取消"),
        BREACHED(5, "已违约");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static Status fromCode(Integer code) {
            for (Status status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown appointment status code: " + code);
        }
    }
}