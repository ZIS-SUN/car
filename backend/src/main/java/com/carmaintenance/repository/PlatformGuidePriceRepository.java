package com.carmaintenance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.PlatformGuidePrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 平台指导价Repository接口
 */
@Mapper
public interface PlatformGuidePriceRepository extends BaseMapper<PlatformGuidePrice> {

    /**
     * 根据服务项目名称查询指导价
     */
    @Select("SELECT * FROM platform_guide_prices WHERE service_name = #{serviceName} AND status = 1")
    PlatformGuidePrice findByServiceName(@Param("serviceName") String serviceName);

    /**
     * 根据分类查询指导价列表
     */
    @Select("SELECT * FROM platform_guide_prices WHERE category = #{category} AND status = 1")
    List<PlatformGuidePrice> findByCategory(@Param("category") String category);

    /**
     * 获取所有有效的指导价
     */
    @Select("SELECT * FROM platform_guide_prices WHERE status = 1 ORDER BY category, service_name")
    List<PlatformGuidePrice> findAllActive();
}
