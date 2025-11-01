package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.dto.ServicePackageDTO;
import com.carmaintenance.entity.*;
import com.carmaintenance.repository.*;
import com.carmaintenance.service.ServicePackageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务套餐Service实现类
 */
@Service
public class ServicePackageServiceImpl extends ServiceImpl<ServicePackageRepository, ServicePackage> implements ServicePackageService {

    @Autowired
    private PackageItemRepository packageItemRepository;

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    @Override
    @Transactional
    public String createServicePackage(ServicePackageDTO packageDTO, Long shopId) {
        // 创建套餐
        ServicePackage servicePackage = new ServicePackage();
        BeanUtils.copyProperties(packageDTO, servicePackage);
        servicePackage.setShopId(shopId);

        if (save(servicePackage)) {
            // 添加套餐项目关联
            if (packageDTO.getItemIds() != null && !packageDTO.getItemIds().isEmpty()) {
                for (Long itemId : packageDTO.getItemIds()) {
                    // 检查项目是否属于该门店
                    ServiceItem item = serviceItemRepository.selectById(itemId);
                    if (item == null || !item.getShopId().equals(shopId)) {
                        throw new RuntimeException("项目ID " + itemId + " 不存在��无权操作");
                    }

                    PackageItem packageItem = new PackageItem();
                    packageItem.setPackageId(servicePackage.getId());
                    packageItem.setItemId(itemId);
                    packageItemRepository.insert(packageItem);
                }
            }

            return "服务套餐创建成功";
        } else {
            throw new RuntimeException("服务套餐创建失败");
        }
    }

    @Override
    @Transactional
    public boolean updateServicePackage(ServicePackageDTO packageDTO, Long shopId) {
        ServicePackage existingPackage = getById(packageDTO.getId());
        if (existingPackage == null) {
            throw new RuntimeException("服务套餐不存在");
        }

        // 检查是否为门店的套餐
        if (!existingPackage.getShopId().equals(shopId)) {
            throw new RuntimeException("无权操作该服务套餐");
        }

        // 更新套餐信息
        BeanUtils.copyProperties(packageDTO, existingPackage, "id", "shopId", "createTime");

        if (updateById(existingPackage)) {
            // 更新套餐项目关联
            // 先删除原有关联
            packageItemRepository.delete(new QueryWrapper<PackageItem>()
                    .eq("package_id", packageDTO.getId()));

            // 添加新的关联
            if (packageDTO.getItemIds() != null && !packageDTO.getItemIds().isEmpty()) {
                for (Long itemId : packageDTO.getItemIds()) {
                    // 检查项目是否属于该门店
                    ServiceItem item = serviceItemRepository.selectById(itemId);
                    if (item == null || !item.getShopId().equals(shopId)) {
                        throw new RuntimeException("项目ID " + itemId + " 不存在或无权操作");
                    }

                    PackageItem packageItem = new PackageItem();
                    packageItem.setPackageId(packageDTO.getId());
                    packageItem.setItemId(itemId);
                    packageItemRepository.insert(packageItem);
                }
            }

            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public boolean deleteServicePackage(Long packageId, Long shopId) {
        ServicePackage packageItem = getById(packageId);
        if (packageItem == null) {
            throw new RuntimeException("服务套餐不存在");
        }

        // 检查是否为门店的套餐
        if (!packageItem.getShopId().equals(shopId)) {
            throw new RuntimeException("无权操作该服务套餐");
        }

        // 删除套餐项目关联
        packageItemRepository.delete(new QueryWrapper<PackageItem>()
                .eq("package_id", packageId));

        // 删除套餐
        return removeById(packageId);
    }

    @Override
    public List<ServicePackage> getShopServicePackages(Long shopId) {
        return baseMapper.findByShopId(shopId);
    }

    @Override
    public Map<String, Object> getServicePackageDetail(Long packageId, Long shopId) {
        ServicePackage servicePackage = getById(packageId);
        if (servicePackage == null) {
            throw new RuntimeException("服务套餐不存在");
        }

        // 检查是否为门店的套餐
        if (!servicePackage.getShopId().equals(shopId)) {
            throw new RuntimeException("无权查看该服务套餐");
        }

        // 获取套餐包含的项目
        List<PackageItem> packageItems = packageItemRepository.findByPackageId(packageId);

        Map<String, Object> result = new HashMap<>();
        result.put("package", servicePackage);
        result.put("items", packageItems);

        return result;
    }

    @Override
    public List<ServicePackage> getPopularPackages(Long shopId) {
        return baseMapper.findPopularPackages(shopId);
    }

    @Override
    @Transactional
    public boolean batchUpdatePackageStatus(List<Long> packageIds, Integer status, Long shopId) {
        // 检查所有套餐是否属于该门店
        List<ServicePackage> packages = listByIds(packageIds);
        for (ServicePackage packageItem : packages) {
            if (!packageItem.getShopId().equals(shopId)) {
                throw new RuntimeException("包含无权操作的服务套餐");
            }
        }

        // 批量更新状态
        for (Long packageId : packageIds) {
            ServicePackage packageItem = getById(packageId);
            packageItem.setStatus(status);
            updateById(packageItem);
        }

        return true;
    }

    @Override
    public Map<String, Object> getServicePackageStats(Long shopId) {
        Map<String, Object> stats = new HashMap<>();

        // 总套餐数
        long totalCount = count(new QueryWrapper<ServicePackage>().eq("shop_id", shopId));
        stats.put("totalCount", totalCount);

        // 上架套餐数
        long activeCount = count(new QueryWrapper<ServicePackage>()
                .eq("shop_id", shopId).eq("status", 1));
        stats.put("activeCount", activeCount);

        // 下架套餐数
        long inactiveCount = count(new QueryWrapper<ServicePackage>()
                .eq("shop_id", shopId).eq("status", 0));
        stats.put("inactiveCount", inactiveCount);

        // 热门套餐
        List<ServicePackage> popularPackages = getPopularPackages(shopId);
        stats.put("popularPackages", popularPackages);

        return stats;
    }
}