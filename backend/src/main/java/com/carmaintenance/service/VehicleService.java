package com.carmaintenance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.dto.VehicleDTO;
import com.carmaintenance.entity.Vehicle;

import java.util.List;

/**
 * 车辆Service接口
 */
public interface VehicleService extends IService<Vehicle> {

    /**
     * 添加车辆
     */
    String addVehicle(VehicleDTO vehicleDTO, Long userId);

    /**
     * 更新车辆信息
     */
    boolean updateVehicle(VehicleDTO vehicleDTO, Long userId);

    /**
     * 删除车辆
     */
    boolean deleteVehicle(Long vehicleId, Long userId);

    /**
     * 获取用户车辆列表
     */
    List<Vehicle> getUserVehicles(Long userId);

    /**
     * 根据ID获取车辆信息
     */
    Vehicle getVehicleById(Long vehicleId, Long userId);

    /**
     * 检查车牌号是否存在
     */
    boolean checkLicensePlateExists(String licensePlate, Long userId);
}