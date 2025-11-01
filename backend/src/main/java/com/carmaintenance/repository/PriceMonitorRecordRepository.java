package com.carmaintenance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.PriceMonitorRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 价格监控记录Repository接口
 */
@Mapper
public interface PriceMonitorRecordRepository extends BaseMapper<PriceMonitorRecord> {

    /**
     * 根据门店ID查询监控记录
     */
    @Select("SELECT * FROM price_monitor_records WHERE shop_id = #{shopId} ORDER BY create_time DESC LIMIT #{limit}")
    List<PriceMonitorRecord> findByShopId(@Param("shopId") Long shopId, @Param("limit") Integer limit);

    /**
     * 查询异常价格记录
     */
    @Select("SELECT * FROM price_monitor_records WHERE status != 0 ORDER BY create_time DESC")
    List<PriceMonitorRecord> findAbnormalRecords();

    /**
     * 统计门店异常价格次数
     */
    @Select("SELECT COUNT(*) FROM price_monitor_records WHERE shop_id = #{shopId} AND status != 0")
    int countAbnormalByShopId(@Param("shopId") Long shopId);
}





