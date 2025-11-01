package com.carmaintenance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.AppointmentItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 预约项目Repository接口
 */
@Mapper
public interface AppointmentItemRepository extends BaseMapper<AppointmentItem> {

    /**
     * 根据预约ID查询项目
     */
    @Select("SELECT * FROM appointment_items WHERE appointment_id = #{appointmentId}")
    List<AppointmentItem> findByAppointmentId(@Param("appointmentId") Long appointmentId);

    /**
     * 批量插入预约项目
     */
    default void insertBatch(List<AppointmentItem> items) {
        if (items != null && !items.isEmpty()) {
            for (AppointmentItem item : items) {
                insert(item);
            }
        }
    }
}