package com.carmaintenance.controller;

import com.carmaintenance.dto.Result;
import com.carmaintenance.dto.ServiceItemDTO;
import com.carmaintenance.entity.ServiceItem;
import com.carmaintenance.entity.Shop;
import com.carmaintenance.security.UserPrincipal;
import com.carmaintenance.service.ServiceItemService;
import com.carmaintenance.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 服务项目控制器
 */
@RestController
@RequestMapping("/service-item")
public class ServiceItemController {

    @Autowired
    private ServiceItemService serviceItemService;

    @Autowired
    private ShopService shopService;

    /**
     * 添加服务项目
     */
    @PostMapping
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> addServiceItem(@Valid @RequestBody ServiceItemDTO itemDTO,
                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 获取门店ID
        Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
        if (shop == null) {
            return Result.error("门店信息不存在");
        }
        
        String message = serviceItemService.addServiceItem(itemDTO, shop.getId());
        return Result.success(message);
    }

    /**
     * 更新服务项目
     */
    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> updateServiceItem(@PathVariable Long itemId,
                                           @Valid @RequestBody ServiceItemDTO itemDTO,
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 获取门店ID
        Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
        if (shop == null) {
            return Result.error("门店信息不存在");
        }
        
        itemDTO.setId(itemId);
        if (serviceItemService.updateServiceItem(itemDTO, shop.getId())) {
            return Result.success("服务项目更新成功");
        } else {
            return Result.error("服务项目更新失败");
        }
    }

    /**
     * 删除服务项目
     */
    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> deleteServiceItem(@PathVariable Long itemId,
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 获取门店ID
        Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
        if (shop == null) {
            return Result.error("门店信息不存在");
        }
        
        if (serviceItemService.deleteServiceItem(itemId, shop.getId())) {
            return Result.success("服务项目删除成功");
        } else {
            return Result.error("服务项目删除失败");
        }
    }

    /**
     * 获取门店服务项目列表
     */
    @GetMapping("/shop/{shopId}")
    public Result<List<ServiceItem>> getShopServiceItems(@PathVariable Long shopId,
                                                        @RequestParam(required = false) String category) {
        List<ServiceItem> items = serviceItemService.getShopServiceItems(shopId, category);
        return Result.success(items);
    }

    /**
     * 获取我的门店服务项目列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('SHOP')")
    public Result<List<ServiceItem>> getMyServiceItems(@RequestParam(required = false) String category,
                                                      @AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 获取门店ID
        Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
        if (shop == null) {
            return Result.error("门店信息不存在");
        }
        
        List<ServiceItem> items = serviceItemService.getShopServiceItems(shop.getId(), category);
        return Result.success(items);
    }

    /**
     * 获取服务项目详情
     */
    @GetMapping("/{itemId}")
    public Result<ServiceItem> getServiceItemDetail(@PathVariable Long itemId,
                                                   @AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 这里需要确定用户是否有权限查看
        // 如果是门店用户，检查是否为门店的项目
        // 如果是车主用户，可以查看所有项目
        ServiceItem item;
        if (userPrincipal != null && userPrincipal.getUserType().equals(2)) {
            // 门店用户
            Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
            if (shop == null) {
                return Result.error("门店信息不存在");
            }
            item = serviceItemService.getServiceItemDetail(itemId, shop.getId());
        } else {
            // 车主用户或未登录用户
            item = serviceItemService.getById(itemId);
        }

        if (item == null) {
            return Result.error("服务项目不存在");
        }

        return Result.success(item);
    }

    /**
     * 获取项目��类列表
     */
    @GetMapping("/categories/{shopId}")
    public Result<List<String>> getServiceCategories(@PathVariable Long shopId) {
        try {
            List<String> categories = serviceItemService.getServiceCategories(shopId);
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量更新项目状态
     */
    @PutMapping("/batch-status")
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> batchUpdateStatus(@RequestBody List<Long> itemIds,
                                          @RequestParam Integer status,
                                          @AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 获取门店ID
        Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
        if (shop == null) {
            return Result.error("门店信息不存在");
        }
        
        if (serviceItemService.batchUpdateStatus(itemIds, status, shop.getId())) {
            return Result.success("批量更新成功");
        } else {
            return Result.error("批量更新失败");
        }
    }

    /**
     * 获取服务项目统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SHOP')")
    public Result<Map<String, Object>> getServiceItemStats(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 获取门店ID
        Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
        if (shop == null) {
            return Result.error("门店信息不存在");
        }
        
        Map<String, Object> stats = serviceItemService.getServiceItemStats(shop.getId());
        return Result.success(stats);
    }
}