package com.carmaintenance.controller;

import com.carmaintenance.dto.Result;
import com.carmaintenance.dto.ServicePackageDTO;
import com.carmaintenance.entity.ServicePackage;
import com.carmaintenance.entity.Shop;
import com.carmaintenance.security.UserPrincipal;
import com.carmaintenance.service.ServicePackageService;
import com.carmaintenance.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务套餐控制器
 */
@RestController
@RequestMapping("/service-package")
public class ServicePackageController {

    @Autowired
    private ServicePackageService servicePackageService;

    @Autowired
    private ShopService shopService;

    /**
     * 创建服务套餐
     */
    @PostMapping
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> createServicePackage(@Valid @RequestBody ServicePackageDTO packageDTO,
                                             @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            // 获取门店ID
            Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
            if (shop == null) {
                return Result.error("门店信息不存在");
            }
            
            String message = servicePackageService.createServicePackage(packageDTO, shop.getId());
            return Result.success(message);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新服务套餐
     */
    @PutMapping("/{packageId}")
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> updateServicePackage(@PathVariable Long packageId,
                                              @Valid @RequestBody ServicePackageDTO packageDTO,
                                              @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            // 获取门店ID
            Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
            if (shop == null) {
                return Result.error("门店信息不存在");
            }
            
            packageDTO.setId(packageId);
            if (servicePackageService.updateServicePackage(packageDTO, shop.getId())) {
                return Result.success("服务套餐更新成功");
            } else {
                return Result.error("服务套餐更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除服务套餐
     */
    @DeleteMapping("/{packageId}")
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> deleteServicePackage(@PathVariable Long packageId,
                                             @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            // 获取门店ID
            Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
            if (shop == null) {
                return Result.error("门店信息不存在");
            }
            
            if (servicePackageService.deleteServicePackage(packageId, shop.getId())) {
                return Result.success("服务套餐删除成功");
            } else {
                return Result.error("服务套餐删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取门店服务套餐列表
     */
    @GetMapping("/shop/{shopId}")
    public Result<List<ServicePackage>> getShopServicePackages(@PathVariable Long shopId) {
        try {
            List<ServicePackage> packages = servicePackageService.getShopServicePackages(shopId);
            return Result.success(packages);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我的门店服务套餐列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('SHOP')")
    public Result<List<ServicePackage>> getMyServicePackages(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            // 获取门店ID
            Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
            if (shop == null) {
                return Result.error("门店信息不存在");
            }
            
            List<ServicePackage> packages = servicePackageService.getShopServicePackages(shop.getId());
            return Result.success(packages);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取服务套餐详情
     */
    @GetMapping("/{packageId}")
    public Result<Map<String, Object>> getServicePackageDetail(@PathVariable Long packageId,
                                                              @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Map<String, Object> detail;
            if (userPrincipal != null && userPrincipal.getUserType().equals(2)) {
                // 门店用户
                Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
                if (shop == null) {
                    return Result.error("门店信息不存在");
                }
                detail = servicePackageService.getServicePackageDetail(packageId, shop.getId());
            } else {
                // 车主用户或未登录用户，获取基本信息
                ServicePackage packageItem = servicePackageService.getById(packageId);
                if (packageItem == null) {
                    return Result.error("服务套餐不存在");
                }
                detail = new HashMap<String, Object>();
                detail.put("package", packageItem);
            }

            return Result.success(detail);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取热门套餐
     */
    @GetMapping("/popular/{shopId}")
    public Result<List<ServicePackage>> getPopularPackages(@PathVariable Long shopId) {
        try {
            List<ServicePackage> packages = servicePackageService.getPopularPackages(shopId);
            return Result.success(packages);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量更新套餐状态
     */
    @PutMapping("/batch-status")
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> batchUpdatePackageStatus(@RequestBody List<Long> packageIds,
                                                  @RequestParam Integer status,
                                                  @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            // 获取门店ID
            Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
            if (shop == null) {
                return Result.error("门店信息不存在");
            }
            
            if (servicePackageService.batchUpdatePackageStatus(packageIds, status, shop.getId())) {
                return Result.success("批量更新成功");
            } else {
                return Result.error("批量更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取服务套餐统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SHOP')")
    public Result<Map<String, Object>> getServicePackageStats(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            // 获取门店ID
            Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
            if (shop == null) {
                return Result.error("门店信息不存在");
            }
            
            Map<String, Object> stats = servicePackageService.getServicePackageStats(shop.getId());
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}