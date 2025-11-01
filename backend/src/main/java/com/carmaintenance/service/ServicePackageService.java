package com.carmaintenance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.dto.ServicePackageDTO;
import com.carmaintenance.entity.ServicePackage;

import java.util.List;
import java.util.Map;

/**
 * 服务套餐Service接口
 */
public interface ServicePackageService extends IService<ServicePackage> {

    /**
     * 创建服务套餐
     */
    String createServicePackage(ServicePackageDTO packageDTO, Long shopId);

    /**
     * 更新服务套餐
     */
    boolean updateServicePackage(ServicePackageDTO packageDTO, Long shopId);

    /**
     * 删��服务套餐
     */
    boolean deleteServicePackage(Long packageId, Long shopId);

    /**
     * 获取门店服务套餐列表
     */
    List<ServicePackage> getShopServicePackages(Long shopId);

    /**
     * 获取套餐详情
     */
    Map<String, Object> getServicePackageDetail(Long packageId, Long shopId);

    /**
     * 获取热门套餐
     */
    List<ServicePackage> getPopularPackages(Long shopId);

    /**
     * 批量更新套餐状态
     */
    boolean batchUpdatePackageStatus(List<Long> packageIds, Integer status, Long shopId);

    /**
     * 获取套餐统计信息
     */
    Map<String, Object> getServicePackageStats(Long shopId);
}