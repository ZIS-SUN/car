package com.carmaintenance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carmaintenance.entity.PlatformGuidePrice;
import com.carmaintenance.entity.PriceMonitorRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 价格监管Service接口
 */
public interface PriceMonitorService {

    /**
     * 创建或更新平台指导价
     */
    boolean saveGuidePrice(PlatformGuidePrice guidePrice);

    /**
     * 删除指导价
     */
    boolean deleteGuidePrice(Long id);

    /**
     * 获取所有指导价
     */
    List<PlatformGuidePrice> getAllGuidePrices();

    /**
     * 根据分类获取指导价
     */
    List<PlatformGuidePrice> getGuidePricesByCategory(String category);

    /**
     * 检查门店服务价格是否异常
     */
    boolean checkServicePrice(Long shopId, String itemName, BigDecimal price);

    /**
     * 记录价格监控
     */
    void recordPriceMonitor(Long shopId, String itemName, BigDecimal shopPrice, BigDecimal guidePrice, String status);

    /**
     * 获取价格监控记录
     */
    Page<PriceMonitorRecord> getMonitorRecords(Page<PriceMonitorRecord> page, Long shopId, String status);

    /**
     * 获取异常价格统计
     */
    Map<String, Object> getAbnormalPriceStats();

    /**
     * 获取门店价格监控统计
     */
    Map<String, Object> getShopPriceStats(Long shopId);
}





