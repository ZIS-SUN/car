package com.carmaintenance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.ExperienceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 经验记录Repository接口
 */
@Mapper
public interface ExperienceRecordRepository extends BaseMapper<ExperienceRecord> {

    /**
     * 根据用户ID查询经验记录
     */
    @Select("SELECT * FROM experience_records WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ExperienceRecord> findByUserId(@Param("userId") Long userId);

    /**
     * 根据订单ID查询经验记录
     */
    @Select("SELECT * FROM experience_records WHERE order_id = #{orderId}")
    ExperienceRecord findByOrderId(@Param("orderId") Long orderId);

    /**
     * 统计用户总经验值
     */
    @Select("SELECT COALESCE(SUM(experience_change), 0) FROM experience_records WHERE user_id = #{userId}")
    Integer getTotalExperience(@Param("userId") Long userId);
}