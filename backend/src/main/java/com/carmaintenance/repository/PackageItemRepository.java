package com.carmaintenance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.PackageItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 套餐项目关联Repository接口
 */
@Mapper
public interface PackageItemRepository extends BaseMapper<PackageItem> {

    /**
     * 根据套餐ID查询项目
     */
    @Select("SELECT pi.*, si.name as item_name, si.category, si.price, si.duration_minutes as duration " +
            "FROM package_items pi " +
            "LEFT JOIN service_items si ON pi.item_id = si.id " +
            "WHERE pi.package_id = #{packageId}")
    List<PackageItem> findByPackageId(@Param("packageId") Long packageId);

    /**
     * 根据项目ID查询相关套餐
     */
    @Select("SELECT DISTINCT package_id FROM package_items WHERE item_id = #{itemId}")
    List<Long> findPackageIdsByItemId(@Param("itemId") Long itemId);
}