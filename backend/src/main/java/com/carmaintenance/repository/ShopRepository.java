package com.carmaintenance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.Shop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 门店Repository接口
 */
@Mapper
public interface ShopRepository extends BaseMapper<Shop> {

    /**
     * 根据城市查询门店
     */
    @Select("SELECT * FROM shops WHERE city = #{city} AND status = 1 ORDER BY create_time DESC")
    List<Shop> findByCity(@Param("city") String city);

    /**
     * 搜索门店
     */
    @Select("SELECT * FROM shops WHERE (shop_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR address LIKE CONCAT('%', #{keyword}, '%')) AND status = 1 ORDER BY create_time DESC")
    List<Shop> searchShops(@Param("keyword") String keyword);

    /**
     * 根据用户ID查询门店
     */
    @Select("SELECT * FROM shops WHERE user_id = #{userId}")
    Shop findByUserId(@Param("userId") Long userId);
}