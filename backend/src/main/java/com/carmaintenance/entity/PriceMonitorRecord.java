package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格监控记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("price_monitor_records")
public class PriceMonitorRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("shop_id")
    private Long shopId;

    @TableField("service_item_id")
    private Long serviceItemId;

    @TableField("service_name")
    private String serviceName;

    @TableField("shop_price")
    private BigDecimal shopPrice;

    @TableField("guide_price")
    private BigDecimal guidePrice;

    @TableField("price_diff")
    private BigDecimal priceDiff; // 价格差异

    @TableField("diff_rate")
    private BigDecimal diffRate; // 差异百分比

    @TableField("status")
    private Integer status; // 0-正常, 1-偏高, 2-偏低

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}





