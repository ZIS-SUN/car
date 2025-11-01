package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.dto.ServiceItemDTO;
import com.carmaintenance.entity.ServiceItem;
import com.carmaintenance.exception.BusinessException;
import com.carmaintenance.repository.ServiceItemRepository;
import com.carmaintenance.service.ServiceItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务项目Service实现类
 */
@Service
public class ServiceItemServiceImpl extends ServiceImpl<ServiceItemRepository, ServiceItem> implements ServiceItemService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceItemServiceImpl.class);

    @Override
    @Transactional
    public String addServiceItem(ServiceItemDTO itemDTO, Long shopId) {
        logger.info("门店添加服务项目: shopId={}, itemName={}", shopId, itemDTO.getItemName());
        
        ServiceItem item = new ServiceItem();
        BeanUtils.copyProperties(itemDTO, item);
        item.setShopId(shopId);
        item.setStatus(1); // 默认上架状态

        if (save(item)) {
            logger.info("服务项目添加成功: shopId={}, itemId={}, itemName={}", 
                       shopId, item.getId(), item.getItemName());
            return "服务项目添加成功";
        } else {
            logger.error("服务项目添加失败: shopId={}, itemName={}", shopId, itemDTO.getItemName());
            throw new BusinessException(500, "服务项目添加失败");
        }
    }

    @Override
    @Transactional
    public boolean updateServiceItem(ServiceItemDTO itemDTO, Long shopId) {
        ServiceItem existingItem = getById(itemDTO.getId());
        if (existingItem == null) {
            throw new BusinessException(404, "服务项目不存在");
        }

        // 检查是否为门店的项目
        if (!existingItem.getShopId().equals(shopId)) {
            logger.warn("服务项目更新失败: 无权操作, shopId={}, itemId={}", shopId, itemDTO.getId());
            throw new BusinessException(403, "无权操作该服务项目");
        }

        BeanUtils.copyProperties(itemDTO, existingItem, "id", "shopId", "createTime");
        return updateById(existingItem);
    }

    @Override
    @Transactional
    public boolean deleteServiceItem(Long itemId, Long shopId) {
        ServiceItem item = getById(itemId);
        if (item == null) {
            throw new BusinessException(404, "服务项目不存在");
        }

        // 检查是否为门店的项目
        if (!item.getShopId().equals(shopId)) {
            logger.warn("服务项目删除失败: 无权操作, shopId={}, itemId={}", shopId, itemId);
            throw new BusinessException(403, "无权操作该服务项目");
        }

        return removeById(itemId);
    }

    @Override
    public List<ServiceItem> getShopServiceItems(Long shopId, String category) {
        QueryWrapper<ServiceItem> wrapper = new QueryWrapper<>();
        wrapper.eq("shop_id", shopId);
        wrapper.eq("status", 1); // 只显示上架的项目

        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq("category", category);
        }

        wrapper.orderByAsc("category", "price");
        return list(wrapper);
    }

    @Override
    public ServiceItem getServiceItemDetail(Long itemId, Long shopId) {
        ServiceItem item = getById(itemId);
        if (item == null) {
            throw new BusinessException(404, "服务项目不存在");
        }

        // 检查是否为门店的项目
        if (!item.getShopId().equals(shopId)) {
            logger.warn("服务项目查看失败: 无权查看, shopId={}, itemId={}", shopId, itemId);
            throw new BusinessException(403, "无权查看该服务项目");
        }

        return item;
    }

    @Override
    public List<String> getServiceCategories(Long shopId) {
        QueryWrapper<ServiceItem> wrapper = new QueryWrapper<>();
        wrapper.eq("shop_id", shopId);
        wrapper.eq("status", 1);
        wrapper.select("DISTINCT category");
        wrapper.groupBy("category");

        List<ServiceItem> items = list(wrapper);
        return items.stream()
                .map(ServiceItem::getCategory)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean batchUpdateStatus(List<Long> itemIds, Integer status, Long shopId) {
        // 检查所有项目是否属于该门店
        List<ServiceItem> items = listByIds(itemIds);
        for (ServiceItem item : items) {
            if (!item.getShopId().equals(shopId)) {
                logger.warn("批量更新状态失败: 包含无权操作的项目, shopId={}, itemId={}", shopId, item.getId());
                throw new BusinessException(403, "包含无权操作的服务项目");
            }
        }

        // 批量更新状态
        for (Long itemId : itemIds) {
            ServiceItem item = getById(itemId);
            item.setStatus(status);
            updateById(item);
        }

        return true;
    }

    @Override
    public Map<String, Object> getServiceItemStats(Long shopId) {
        Map<String, Object> stats = new HashMap<>();

        // 总项目数
        long totalCount = count(new QueryWrapper<ServiceItem>().eq("shop_id", shopId));
        stats.put("totalCount", totalCount);

        // 上架项目数
        long activeCount = count(new QueryWrapper<ServiceItem>()
                .eq("shop_id", shopId).eq("status", 1));
        stats.put("activeCount", activeCount);

        // 下架项目数
        long inactiveCount = count(new QueryWrapper<ServiceItem>()
                .eq("shop_id", shopId).eq("status", 0));
        stats.put("inactiveCount", inactiveCount);

        // 分类统计
        List<ServiceItem> items = list(new QueryWrapper<ServiceItem>()
                .eq("shop_id", shopId).eq("status", 1));
        Map<String, Long> categoryStats = items.stream()
                .collect(Collectors.groupingBy(ServiceItem::getCategory, Collectors.counting()));
        stats.put("categoryStats", categoryStats);

        // 价格统计
        if (!items.isEmpty()) {
            double avgPrice = items.stream()
                    .mapToDouble(item -> item.getPrice().doubleValue())
                    .average()
                    .orElse(0.0);
            double minPrice = items.stream()
                    .mapToDouble(item -> item.getPrice().doubleValue())
                    .min()
                    .orElse(0.0);
            double maxPrice = items.stream()
                    .mapToDouble(item -> item.getPrice().doubleValue())
                    .max()
                    .orElse(0.0);

            Map<String, Object> priceStats = new HashMap<>();
            priceStats.put("avgPrice", avgPrice);
            priceStats.put("minPrice", minPrice);
            priceStats.put("maxPrice", maxPrice);
            stats.put("priceStats", priceStats);
        }

        return stats;
    }
}