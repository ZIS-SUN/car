package com.carmaintenance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.dto.AppointmentDTO;
import com.carmaintenance.entity.Appointment;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 预约Service接口
 */
public interface AppointmentService extends IService<Appointment> {

    /**
     * 创建预约
     */
    String createAppointment(AppointmentDTO appointmentDTO, Long userId);

    /**
     * 取消预约
     */
    boolean cancelAppointment(Long appointmentId, Long userId, String reason);

    /**
     * 修改预约
     */
    boolean updateAppointment(Long appointmentId, AppointmentDTO appointmentDTO, Long userId);

    /**
     * 获取用户预约列表
     */
    Page<Appointment> getUserAppointments(Page<Appointment> page, Long userId, Integer status);

    /**
     * 获取门店预约列表
     */
    Page<Appointment> getShopAppointments(Page<Appointment> page, Long shopId, Integer status, LocalDate date);

    /**
     * 获取预约详情
     */
    Map<String, Object> getAppointmentDetail(Long appointmentId, Long userId);

    /**
     * 检查时间段是否可用
     */
    boolean checkTimeSlotAvailable(Long shopId, LocalDate date, String timeSlot);

    /**
     * 获取可用时间段
     */
    List<String> getAvailableTimeSlots(Long shopId, LocalDate date);

    /**
     * 更新预约状态
     */
    boolean updateAppointmentStatus(Long appointmentId, Integer status);

    /**
     * 分配工位
     */
    boolean assignBay(Long appointmentId, Integer bayNumber);

    /**
     * 获取预约统计信息
     */
    Map<String, Object> getAppointmentStats(Long shopId);
}