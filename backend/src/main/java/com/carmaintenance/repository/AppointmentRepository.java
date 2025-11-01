package com.carmaintenance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约Repository接口
 */
@Mapper
public interface AppointmentRepository extends BaseMapper<Appointment> {

    /**
     * 根据用户ID查询预约
     */
    @Select("SELECT * FROM appointments WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Appointment> findByUserId(@Param("userId") Long userId);

    /**
     * 根据门店ID查询预约
     */
    @Select("SELECT * FROM appointments WHERE shop_id = #{shopId} ORDER BY create_time DESC")
    List<Appointment> findByShopId(@Param("shopId") Long shopId);

    /**
     * 查询指定日期的预约
     */
    @Select("SELECT * FROM appointments WHERE shop_id = #{shopId} AND appointment_date = #{date} AND status IN (1,2) ORDER BY time_slot")
    List<Appointment> findByShopIdAndDate(@Param("shopId") Long shopId, @Param("date") LocalDate date);

    /**
     * 检查时间段是否已被预约
     */
    @Select("SELECT COUNT(*) FROM appointments WHERE shop_id = #{shopId} AND appointment_date = #{date} AND time_slot = #{timeSlot} AND status IN (1,2)")
    int countByShopIdAndDateTime(@Param("shopId") Long shopId, @Param("date") LocalDate date, @Param("timeSlot") String timeSlot);

    /**
     * 根据预约编号查询
     */
    @Select("SELECT * FROM appointments WHERE appointment_no = #{appointmentNo}")
    Appointment findByAppointmentNo(@Param("appointmentNo") String appointmentNo);

    /**
     * 获取用户预约统计
     */
    @Select("SELECT COUNT(*) FROM appointments WHERE user_id = #{userId} AND status = #{status}")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);
}