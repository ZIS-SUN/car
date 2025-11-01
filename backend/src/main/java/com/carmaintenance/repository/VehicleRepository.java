package com.carmaintenance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.Vehicle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 车辆Repository接口
 */
@Mapper
public interface VehicleRepository extends BaseMapper<Vehicle> {

    /**
     * 根据用户查询车辆列表
     */
    @Select("SELECT * FROM vehicles WHERE user_id = #{userId} ORDER BY id DESC")
    List<Vehicle> findByUserId(@Param("userId") Long userId);

    /**
     * 检查车牌是否已存在
     */
    @Select("SELECT COUNT(*) FROM vehicles WHERE license_plate = #{licensePlate} AND user_id = #{userId}")
    int checkLicensePlateExists(@Param("licensePlate") String licensePlate, @Param("userId") Long userId);
}