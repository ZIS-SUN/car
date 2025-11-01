package com.carmaintenance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.dto.ServiceItemDTO;
import com.carmaintenance.entity.ServiceItem;

import java.util.List;
import java.util.Map;

/**
 * 服务项目Service接口
 */
public interface ServiceItemService extends IService<ServiceItem> {

    /**
     * 添加服务项目
     */
    String addServiceItem(ServiceItemDTO itemDTO, Long shopId);

    /**
     * 更新服务项目
     */
    boolean updateServiceItem(ServiceItemDTO itemDTO, Long shopId);

    /**
     * 删除服务项目
     */
    boolean deleteServiceItem(Long itemId, Long shopId);

    /**
     * 获取门店服务项目列表
     */
    List<ServiceItem> getShopServiceItems(Long shopId, String category);

    /**
     * 获取项目详情
     */
    ServiceItem getServiceItemDetail(Long itemId, Long shopId);

    /**
     * 获取项目分类列表
     */
    List<String> getServiceCategories(Long shopId);

    /**
     * 批量更新项目状态
     */
    boolean batchUpdateStatus(List<Long> itemIds, Integer status, Long shopId);

    /**
     * 获取项目统计信息
     */
    Map<String, Object> getServiceItemStats(Long shopId);
}