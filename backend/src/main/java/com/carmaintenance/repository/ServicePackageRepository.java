package com.carmaintenance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.ServicePackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 服务套餐Repository接口
 */
@Mapper
public interface ServicePackageRepository extends BaseMapper<ServicePackage> {

    /**
     * 根据门店ID查询套餐
     */
    @Select("SELECT id, shop_id, package_name, description, package_price, original_price, status, image_url, create_time, update_time FROM service_packages WHERE shop_id = #{shopId} AND status = 1 ORDER BY package_price")
    List<ServicePackage> findByShopId(@Param("shopId") Long shopId);

    /**
     * 获取热门套餐
     */
    @Select("SELECT sp.id, sp.shop_id, sp.package_name, sp.description, sp.package_price, sp.original_price, sp.status, sp.image_url, sp.create_time, sp.update_time " +
            "FROM service_packages sp " +
            "JOIN orders o ON sp.shop_id = o.shop_id " +
            "WHERE sp.shop_id = #{shopId} AND sp.status = 1 " +
            "GROUP BY sp.id ORDER BY COUNT(o.id) DESC LIMIT 5")
    List<ServicePackage> findPopularPackages(@Param("shopId") Long shopId);
}