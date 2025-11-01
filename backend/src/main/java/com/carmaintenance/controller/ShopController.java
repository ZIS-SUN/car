package com.carmaintenance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carmaintenance.dto.Result;
import com.carmaintenance.dto.ShopRegisterDTO;
import com.carmaintenance.entity.Shop;
import com.carmaintenance.security.UserPrincipal;
import com.carmaintenance.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 门店控制器
 */
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    /**
     * 门店注册
     */
    @PostMapping("/register")
    public Result<String> registerShop(@Valid @RequestBody ShopRegisterDTO registerDTO) {
        String message = shopService.registerShop(registerDTO);
        return Result.success(message);
    }

    /**
     * 获取门店列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<Shop>> getShopList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        Page<Shop> page = new Page<>(current, size);
        Page<Shop> shopPage = shopService.getShopPage(page, city, keyword, status);

        return Result.success(shopPage);
    }

    /**
     * 根据城市获取门店列表
     */
    @GetMapping("/city/{city}")
    public Result<List<Shop>> getShopsByCity(@PathVariable String city) {
        List<Shop> shops = shopService.getShopsByCity(city);
        return Result.success(shops);
    }

    /**
     * 搜索门店
     */
    @GetMapping("/search")
    public Result<List<Shop>> searchShops(@RequestParam String keyword) {
        List<Shop> shops = shopService.searchShops(keyword);
        return Result.success(shops);
    }

    /**
     * 获取门店详情
     */
    @GetMapping("/{shopId}")
    public Result<Shop> getShopDetail(@PathVariable Long shopId) {
        Shop shop = shopService.getById(shopId);
        if (shop == null) {
            return Result.error("门店不存在");
        }

        return Result.success(shop);
    }

    /**
     * 获取我的门店信息（门店用户）
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('SHOP')")
    public Result<Shop> getMyShop(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 添加调试日志
        System.out.println("=== 门店信息调试 ===");
        System.out.println("UserPrincipal: " + userPrincipal);
        if (userPrincipal != null) {
            System.out.println("UserId: " + userPrincipal.getUserId());
            System.out.println("Username: " + userPrincipal.getUsername());
            System.out.println("UserType: " + userPrincipal.getUserType());
        }
        
        Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
        System.out.println("查询到的门店: " + shop);
        
        if (shop == null) {
            return Result.error("门店信息不存在");
        }

        return Result.success(shop);
    }

    /**
     * 更新门店信息（门店用户）
     */
    @PutMapping("/my")
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> updateMyShop(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                      @RequestBody Shop shop) {
        // 获取用户的门店信息
        Shop myShop = shopService.getShopByUserId(userPrincipal.getUserId());
        if (myShop == null) {
            return Result.error("门店信息不存在");
        }

        shop.setId(myShop.getId());
        if (shopService.updateShopInfo(myShop.getId(), shop)) {
            return Result.success("门店信息更新成功");
        } else {
            return Result.error("门店信息更新失败");
        }
    }

    /**
     * 更新门店状态（管理员）
     */
    @PutMapping("/{shopId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateShopStatus(@PathVariable Long shopId,
                                          @RequestParam Integer status) {
        if (shopService.updateShopStatus(shopId, status)) {
            String statusText = status == 1 ? "审核通过" : "审核拒绝";
            return Result.success("门店" + statusText);
        } else {
            return Result.error("状态更新失败");
        }
    }

    /**
     * 获取门店统计信息（门店用户）
     */
    @GetMapping("/my/stats")
    @PreAuthorize("hasRole('SHOP')")
    public Result<Shop> getShopStats(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
        if (shop == null) {
            return Result.error("门店信息不存在");
        }

        Shop stats = shopService.getShopStats(shop.getId());
        return Result.success(stats);
    }
}