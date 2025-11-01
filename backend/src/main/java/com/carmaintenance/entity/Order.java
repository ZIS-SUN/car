package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("orders")
public class Order {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("appointment_id")
    private Long appointmentId;

    @TableField("user_id")
    private Long userId;

    @TableField("shop_id")
    private Long shopId;

    @TableField("vehicle_id")
    private Long vehicleId;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("discount_amount")
    private BigDecimal discountAmount;

    @TableField("final_amount")
    private BigDecimal finalAmount;

    @TableField("payment_method")
    private Integer paymentMethod; // 1-现金, 2-微信, 3-支付宝, 4-银行卡

    @TableField("payment_status")
    private Integer paymentStatus; // 0-未支付, 1-已支付

    @TableField("technician_id")
    private Long technicianId;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("status")
    private Integer status; // 1-待服务, 2-进行中, 3-已完成, 4-已取消

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public enum PaymentMethod {
        CASH(1, "现金"),
        WECHAT(2, "微信支付"),
        ALIPAY(3, "支付宝"),
        BANK_CARD(4, "银行卡");

        private final Integer code;
        private final String desc;

        PaymentMethod(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum Status {
        PENDING(1, "待服务"),
        IN_PROGRESS(2, "进行中"),
        COMPLETED(3, "已完成"),
        CANCELLED(4, "已取消");

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
            throw new IllegalArgumentException("Unknown order status code: " + code);
        }
    }
}