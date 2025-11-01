package com.carmaintenance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.dto.ShopRegisterDTO;
import com.carmaintenance.entity.Shop;

import java.util.List;

/**
 * 门店Service接口
 */
public interface ShopService extends IService<Shop> {

    /**
     * 门店注册
     */
    String registerShop(ShopRegisterDTO registerDTO);

    /**
     * 根据城市查询门店
     */
    List<Shop> getShopsByCity(String city);

    /**
     * 搜索门店
     */
    List<Shop> searchShops(String keyword);

    /**
     * 分页查询门店列表
     */
    Page<Shop> getShopPage(Page<Shop> page, String city, String keyword, Integer status);

    /**
     * 更新门店状态
     */
    boolean updateShopStatus(Long shopId, Integer status);

    /**
     * 根据用户ID获取门店信息
     */
    Shop getShopByUserId(Long userId);

    /**
     * 更新门店信息
     */
    boolean updateShopInfo(Long shopId, Shop shop);

    /**
     * 获取门店统计信息
     */
    Shop getShopStats(Long shopId);

    /**
     * 门店认证登录
     */
    Shop authenticateShop(String username, String password);

    /**
     * 修改密码
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
}