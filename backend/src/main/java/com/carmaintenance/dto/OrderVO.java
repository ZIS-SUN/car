package com.carmaintenance.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单视图对象 - 包含关联数据
 */
@Data
public class OrderVO {

    private Long id;

    private String orderNo;

    private Long appointmentId;

    private Long userId;

    private Long shopId;

    // 门店信息
    private String shopName;

    private String shopAddress;

    private String shopPhone;

    // 车辆信息
    private Long vehicleId;

    private String vehicleBrand;

    private String vehicleModel;

    private String licensePlate;

    // 预约信息
    private String appointmentDate;

    private String appointmentTime;

    // 服务项目信息
    private String serviceItems;

    // 订单金额
    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal finalAmount;

    // 支付信息
    private Integer paymentMethod;

    private Integer paymentStatus;

    // 技师信息
    private Long technicianId;

    private String technicianName;

    // 服务时间
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    // 订单状态
    private Integer status;

    private String statusText;

    // 时间戳
    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
