package com.carmaintenance.controller;

import com.carmaintenance.dto.Result;
import com.carmaintenance.dto.ShopLoginDTO;
import com.carmaintenance.entity.Shop;
import com.carmaintenance.security.UserPrincipal;
import com.carmaintenance.service.ShopService;
import com.carmaintenance.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 门店认证控制器
 */
@RestController
@RequestMapping("/shop/auth")
public class ShopAuthController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 门店登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody ShopLoginDTO loginDTO) {
        try {
            // 门店登录验证
            Shop shop = shopService.authenticateShop(loginDTO.getUsername(), loginDTO.getPassword());
            
            if (shop == null) {
                return Result.error("用户名或密码错误");
            }

            // 检查门店状态
            if (shop.getStatus() != 1) {
                return Result.error("门店账户已被禁用，请联系管理员");
            }

            // 生成JWT token
            String token = jwtUtils.generateToken(shop.getUserId(), shop.getShopName(), 2); // 2 = SHOP role
            System.out.println("生成的token: " + token);

            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("shop", shop);

            return Result.success("登录成功", data);
        } catch (Exception e) {
            System.out.println("登录异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 门店退出登录
     */
    @PostMapping("/logout")
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> logout(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 这里可以添加token黑名单等逻辑
        return Result.success("退出登录成功");
    }

    /**
     * 获取门店信息
     */
    @GetMapping("/info")
    @PreAuthorize("hasRole('SHOP')")
    public Result<Shop> getShopInfo(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
        if (shop == null) {
            return Result.error("门店信息不存在");
        }
        return Result.success(shop);
    }

    /**
     * 更新门店信息
     */
    @PutMapping("/info")
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> updateShopInfo(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                         @RequestBody Shop shop) {
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
     * 修改密码
     */
    @PutMapping("/password")
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> changePassword(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                        @RequestBody Map<String, String> passwordData) {
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return Result.error("请输入原密码和新密码");
        }

        try {
            if (shopService.changePassword(userPrincipal.getUserId(), oldPassword, newPassword)) {
                return Result.success("密码修改成功");
            } else {
                return Result.error("原密码错误或修改失败");
            }
        } catch (Exception e) {
            return Result.error("密码修改失败：" + e.getMessage());
        }
    }
}