package com.carmaintenance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carmaintenance.dto.Result;
import com.carmaintenance.entity.PlatformGuidePrice;
import com.carmaintenance.entity.PriceMonitorRecord;
import com.carmaintenance.service.PriceMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 价格监管控制器
 */
@RestController
@RequestMapping("/admin/price-monitor")
@PreAuthorize("hasRole('ADMIN')")
public class PriceMonitorController {

    @Autowired
    private PriceMonitorService priceMonitorService;

    /**
     * 获取所有平台指导价
     */
    @GetMapping("/guide-prices")
    public Result<List<PlatformGuidePrice>> getAllGuidePrices() {
        List<PlatformGuidePrice> prices = priceMonitorService.getAllGuidePrices();
        return Result.success(prices);
    }

    /**
     * 根据分类获取指导价
     */
    @GetMapping("/guide-prices/category/{category}")
    public Result<List<PlatformGuidePrice>> getGuidePricesByCategory(@PathVariable String category) {
        List<PlatformGuidePrice> prices = priceMonitorService.getGuidePricesByCategory(category);
        return Result.success(prices);
    }

    /**
     * 创建或更新平台指导价
     */
    @PostMapping("/guide-prices")
    public Result<String> saveGuidePrice(@Valid @RequestBody PlatformGuidePrice guidePrice) {
        if (priceMonitorService.saveGuidePrice(guidePrice)) {
            return Result.success(guidePrice.getId() != null ? "更新成功" : "创建成功");
        }
        return Result.error("操作失败");
    }

    /**
     * 更新平台指导价
     */
    @PutMapping("/guide-prices/{id}")
    public Result<String> updateGuidePrice(@PathVariable Long id, 
                                          @Valid @RequestBody PlatformGuidePrice guidePrice) {
        guidePrice.setId(id);
        if (priceMonitorService.saveGuidePrice(guidePrice)) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除平台指导价
     */
    @DeleteMapping("/guide-prices/{id}")
    public Result<String> deleteGuidePrice(@PathVariable Long id) {
        if (priceMonitorService.deleteGuidePrice(id)) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 检查门店服务价格
     */
    @GetMapping("/check-price")
    public Result<Map<String, Object>> checkPrice(@RequestParam Long shopId,
                                                  @RequestParam String itemName,
                                                  @RequestParam BigDecimal price) {
        boolean isNormal = priceMonitorService.checkServicePrice(shopId, itemName, price);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("isNormal", isNormal);
        result.put("message", isNormal ? "价格正常" : "价格异常，请核查");
        return Result.success(result);
    }

    /**
     * 获取价格监控记录
     */
    @GetMapping("/records")
    public Result<Page<PriceMonitorRecord>> getMonitorRecords(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) String status) {
        
        Page<PriceMonitorRecord> page = new Page<>(current, size);
        Page<PriceMonitorRecord> result = priceMonitorService.getMonitorRecords(page, shopId, status);
        return Result.success(result);
    }

    /**
     * 获取异常价格统计
     */
    @GetMapping("/abnormal-stats")
    public Result<Map<String, Object>> getAbnormalStats() {
        Map<String, Object> stats = priceMonitorService.getAbnormalPriceStats();
        return Result.success(stats);
    }

    /**
     * 获取门店价格统计
     */
    @GetMapping("/shop-stats/{shopId}")
    public Result<Map<String, Object>> getShopPriceStats(@PathVariable Long shopId) {
        Map<String, Object> stats = priceMonitorService.getShopPriceStats(shopId);
        return Result.success(stats);
    }
}





