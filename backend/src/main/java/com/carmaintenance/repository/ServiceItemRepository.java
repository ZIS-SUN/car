package com.carmaintenance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.ServiceItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 服务项目Repository接口
 */
@Mapper
public interface ServiceItemRepository extends BaseMapper<ServiceItem> {

    /**
     * 根据门店ID查询服务项目
     */
    @Select("SELECT * FROM service_items WHERE shop_id = #{shopId} AND status = 1 ORDER BY category, price")
    List<ServiceItem> findByShopId(@Param("shopId") Long shopId);

    /**
     * 根据分类查询服务项目
     */
    @Select("SELECT * FROM service_items WHERE shop_id = #{shopId} AND category = #{category} AND status = 1 ORDER BY price")
    List<ServiceItem> findByShopIdAndCategory(@Param("shopId") Long shopId, @Param("category") String category);
}