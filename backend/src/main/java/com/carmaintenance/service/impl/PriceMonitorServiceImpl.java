package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carmaintenance.entity.PlatformGuidePrice;
import com.carmaintenance.entity.PriceMonitorRecord;
import com.carmaintenance.repository.PlatformGuidePriceRepository;
import com.carmaintenance.repository.PriceMonitorRecordRepository;
import com.carmaintenance.service.PriceMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 价格监管Service实现类
 */
@Service
public class PriceMonitorServiceImpl implements PriceMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(PriceMonitorServiceImpl.class);

    @Autowired
    private PlatformGuidePriceRepository guidePriceRepository;

    @Autowired
    private PriceMonitorRecordRepository monitorRecordRepository;

    // 价格偏差阈值（超过30%视为异常）
    private static final BigDecimal PRICE_DEVIATION_THRESHOLD = new BigDecimal("0.30");

    @Override
    public boolean saveGuidePrice(PlatformGuidePrice guidePrice) {
        if (guidePrice.getId() != null) {
            // 更新
            return guidePriceRepository.updateById(guidePrice) > 0;
        } else {
            // 新增
            return guidePriceRepository.insert(guidePrice) > 0;
        }
    }

    @Override
    public boolean deleteGuidePrice(Long id) {
        PlatformGuidePrice guidePrice = guidePriceRepository.selectById(id);
        if (guidePrice != null) {
            guidePrice.setStatus(0); // 设置为无效
            return guidePriceRepository.updateById(guidePrice) > 0;
        }
        return false;
    }

    @Override
    public List<PlatformGuidePrice> getAllGuidePrices() {
        return guidePriceRepository.findAllActive();
    }

    @Override
    public List<PlatformGuidePrice> getGuidePricesByCategory(String category) {
        return guidePriceRepository.findByCategory(category);
    }

    @Override
    public boolean checkServicePrice(Long shopId, String itemName, BigDecimal shopPrice) {
        // 查询平台指导价
        PlatformGuidePrice guidePrice = guidePriceRepository.findByServiceName(itemName);
        
        if (guidePrice == null) {
            logger.warn("未找到服务项目的指导价: {}", itemName);
            return true; // 没有指导价则不检查
        }

        // 计算价格偏差率
        BigDecimal minPrice = guidePrice.getMinPrice();
        BigDecimal maxPrice = guidePrice.getMaxPrice();
        
        boolean isNormal = true;
        String status = "NORMAL";

        // 判断是否超出指导价范围
        if (shopPrice.compareTo(minPrice) < 0) {
            // 低于最低指导价
            BigDecimal deviation = minPrice.subtract(shopPrice)
                    .divide(minPrice, 4, RoundingMode.HALF_UP);
            if (deviation.compareTo(PRICE_DEVIATION_THRESHOLD) > 0) {
                isNormal = false;
                status = "TOO_LOW";
            }
        } else if (shopPrice.compareTo(maxPrice) > 0) {
            // 高于最高指导价
            BigDecimal deviation = shopPrice.subtract(maxPrice)
                    .divide(maxPrice, 4, RoundingMode.HALF_UP);
            if (deviation.compareTo(PRICE_DEVIATION_THRESHOLD) > 0) {
                isNormal = false;
                status = "TOO_HIGH";
            }
        }

        // 记录监控
        if (!isNormal) {
            recordPriceMonitor(shopId, itemName, shopPrice, guidePrice.getGuidePrice(), status);
        }

        return isNormal;
    }

    @Override
    public void recordPriceMonitor(Long shopId, String itemName, BigDecimal shopPrice, 
                                   BigDecimal guidePrice, String status) {
        PriceMonitorRecord record = new PriceMonitorRecord();
        record.setShopId(shopId);
        record.setServiceName(itemName);
        record.setShopPrice(shopPrice);
        record.setGuidePrice(guidePrice);
        
        // 转换状态字符串为整数
        Integer statusCode = 0; // 默认正常
        if ("TOO_HIGH".equals(status)) {
            statusCode = 1;
        } else if ("TOO_LOW".equals(status)) {
            statusCode = 2;
        }
        record.setStatus(statusCode);

        // 计算价格差异和差异率
        if (guidePrice != null && guidePrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal priceDiff = shopPrice.subtract(guidePrice);
            record.setPriceDiff(priceDiff);
            
            BigDecimal diffRate = priceDiff.divide(guidePrice, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            record.setDiffRate(diffRate);
        }

        monitorRecordRepository.insert(record);
        logger.info("记录价格监控: shopId={}, itemName={}, status={}", shopId, itemName, status);
    }

    @Override
    public Page<PriceMonitorRecord> getMonitorRecords(Page<PriceMonitorRecord> page, Long shopId, String status) {
        QueryWrapper<PriceMonitorRecord> wrapper = new QueryWrapper<>();
        
        if (shopId != null) {
            wrapper.eq("shop_id", shopId);
        }
        
        if (status != null && !status.isEmpty()) {
            // 转换状态字符串为整数
            Integer statusCode = 0;
            if ("TOO_HIGH".equals(status)) {
                statusCode = 1;
            } else if ("TOO_LOW".equals(status)) {
                statusCode = 2;
            }
            wrapper.eq("status", statusCode);
        }
        
        wrapper.orderByDesc("create_time");
        
        return monitorRecordRepository.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getAbnormalPriceStats() {
        Map<String, Object> stats = new HashMap<>();

        // 统计异常记录总数 (status != 0)
        QueryWrapper<PriceMonitorRecord> wrapper = new QueryWrapper<>();
        wrapper.ne("status", 0);
        long totalAbnormal = monitorRecordRepository.selectCount(wrapper);
        stats.put("totalAbnormal", totalAbnormal);

        // 统计过高价格数量 (status = 1)
        wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        long tooHighCount = monitorRecordRepository.selectCount(wrapper);
        stats.put("tooHighCount", tooHighCount);

        // 统计过低价格数量 (status = 2)
        wrapper = new QueryWrapper<>();
        wrapper.eq("status", 2);
        long tooLowCount = monitorRecordRepository.selectCount(wrapper);
        stats.put("tooLowCount", tooLowCount);

        // 获取最近的异常记录
        List<PriceMonitorRecord> recentAbnormal = monitorRecordRepository.findAbnormalRecords();
        if (recentAbnormal.size() > 10) {
            recentAbnormal = recentAbnormal.subList(0, 10);
        }
        stats.put("recentAbnormal", recentAbnormal);

        return stats;
    }

    @Override
    public Map<String, Object> getShopPriceStats(Long shopId) {
        Map<String, Object> stats = new HashMap<>();

        // 统计该门店的异常次数
        int abnormalCount = monitorRecordRepository.countAbnormalByShopId(shopId);
        stats.put("abnormalCount", abnormalCount);

        // 获取最近的监控记录
        List<PriceMonitorRecord> recentRecords = monitorRecordRepository.findByShopId(shopId, 20);
        stats.put("recentRecords", recentRecords);

        return stats;
    }
}

