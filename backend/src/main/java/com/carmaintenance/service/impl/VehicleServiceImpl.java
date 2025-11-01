package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.dto.VehicleDTO;
import com.carmaintenance.entity.Vehicle;
import com.carmaintenance.exception.BusinessException;
import com.carmaintenance.repository.VehicleRepository;
import com.carmaintenance.service.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 车辆Service实现类
 */
@Service
public class VehicleServiceImpl extends ServiceImpl<VehicleRepository, Vehicle> implements VehicleService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleServiceImpl.class);

    @Override
    @Transactional
    public String addVehicle(VehicleDTO vehicleDTO, Long userId) {
        logger.info("用户添加车辆: userId={}, licensePlate={}", userId, vehicleDTO.getLicensePlate());
        
        // 检查车牌号是否已存在
        if (checkLicensePlateExists(vehicleDTO.getLicensePlate(), userId)) {
            logger.warn("车辆添加失败: 车牌号已存在, userId={}, licensePlate={}", userId, vehicleDTO.getLicensePlate());
            throw new BusinessException(400, "该车牌号已存在");
        }

        Vehicle vehicle = new Vehicle();
        BeanUtils.copyProperties(vehicleDTO, vehicle);
        vehicle.setUserId(userId);

        if (save(vehicle)) {
            logger.info("车辆添加成功: userId={}, vehicleId={}, licensePlate={}", userId, vehicle.getId(), vehicle.getLicensePlate());
            return "车辆添加成功";
        } else {
            logger.error("车辆添加失败: 数据库保存失败, userId={}", userId);
            throw new BusinessException(500, "车辆添加失败");
        }
    }

    @Override
    @Transactional
    public boolean updateVehicle(VehicleDTO vehicleDTO, Long userId) {
        Vehicle existingVehicle = getById(vehicleDTO.getId());
        if (existingVehicle == null) {
            throw new BusinessException(404, "车辆不存在");
        }

        // 检查是否为车主的车辆
        if (!existingVehicle.getUserId().equals(userId)) {
            logger.warn("车辆更新失败: 无权操作, userId={}, vehicleId={}", userId, vehicleDTO.getId());
            throw new BusinessException(403, "无权操作该车辆");
        }

        // 如果车牌号有变化，检查新车牌号是否已存在
        if (!existingVehicle.getLicensePlate().equals(vehicleDTO.getLicensePlate())) {
            if (checkLicensePlateExists(vehicleDTO.getLicensePlate(), userId)) {
                logger.warn("车辆更新失败: 新车牌号已存在, userId={}, licensePlate={}", userId, vehicleDTO.getLicensePlate());
                throw new BusinessException(400, "该车牌号已存在");
            }
        }

        BeanUtils.copyProperties(vehicleDTO, existingVehicle, "id", "userId", "createTime");

        return updateById(existingVehicle);
    }

    @Override
    @Transactional
    public boolean deleteVehicle(Long vehicleId, Long userId) {
        Vehicle vehicle = getById(vehicleId);
        if (vehicle == null) {
            throw new BusinessException(404, "车辆不存在");
        }

        // 检查是否为车主的车辆
        if (!vehicle.getUserId().equals(userId)) {
            logger.warn("车辆删除失败: 无权操作, userId={}, vehicleId={}", userId, vehicleId);
            throw new BusinessException(403, "无权操作该车辆");
        }

        return removeById(vehicleId);
    }

    @Override
    public List<Vehicle> getUserVehicles(Long userId) {
        return baseMapper.findByUserId(userId);
    }

    @Override
    public Vehicle getVehicleById(Long vehicleId, Long userId) {
        Vehicle vehicle = getById(vehicleId);
        if (vehicle == null) {
            throw new BusinessException(404, "车辆不存在");
        }

        // 检查是否为车主的车辆
        if (!vehicle.getUserId().equals(userId)) {
            logger.warn("车辆查看失败: 无权查看, userId={}, vehicleId={}", userId, vehicleId);
            throw new BusinessException(403, "无权查看该车辆");
        }

        return vehicle;
    }

    @Override
    public boolean checkLicensePlateExists(String licensePlate, Long userId) {
        int count = baseMapper.checkLicensePlateExists(licensePlate, userId);
        return count > 0;
    }
}