package com.carmaintenance.controller;

import com.carmaintenance.dto.Result;
import com.carmaintenance.dto.VehicleDTO;
import com.carmaintenance.entity.Vehicle;
import com.carmaintenance.security.UserPrincipal;
import com.carmaintenance.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 车辆控制器
 */
@RestController
@RequestMapping("/vehicle")
@PreAuthorize("hasRole('CUSTOMER')")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    /**
     * 添加车辆
     */
    @PostMapping
    public Result<String> addVehicle(@Valid @RequestBody VehicleDTO vehicleDTO,
                                    @AuthenticationPrincipal UserPrincipal userPrincipal) {
        String message = vehicleService.addVehicle(vehicleDTO, userPrincipal.getUserId());
        return Result.success(message);
    }

    /**
     * 更新车辆信息
     */
    @PutMapping("/{vehicleId}")
    public Result<String> updateVehicle(@PathVariable Long vehicleId,
                                       @Valid @RequestBody VehicleDTO vehicleDTO,
                                       @AuthenticationPrincipal UserPrincipal userPrincipal) {
        vehicleDTO.setId(vehicleId);
        if (vehicleService.updateVehicle(vehicleDTO, userPrincipal.getUserId())) {
            return Result.success("车辆信息更新成功");
        } else {
            return Result.error("车辆信息更新失败");
        }
    }

    /**
     * 删除车辆
     */
    @DeleteMapping("/{vehicleId}")
    public Result<String> deleteVehicle(@PathVariable Long vehicleId,
                                       @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (vehicleService.deleteVehicle(vehicleId, userPrincipal.getUserId())) {
            return Result.success("车辆删除成功");
        } else {
            return Result.error("车辆删除失败");
        }
    }

    /**
     * 获取用户车辆列表
     */
    @GetMapping
    public Result<List<Vehicle>> getUserVehicles(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<Vehicle> vehicles = vehicleService.getUserVehicles(userPrincipal.getUserId());
        return Result.success(vehicles);
    }

    /**
     * 获取车辆详情
     */
    @GetMapping("/{vehicleId}")
    public Result<Vehicle> getVehicleDetail(@PathVariable Long vehicleId,
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Vehicle vehicle = vehicleService.getVehicleById(vehicleId, userPrincipal.getUserId());
        return Result.success(vehicle);
    }

    /**
     * 检查车牌号是否存在
     */
    @GetMapping("/check-license")
    public Result<Boolean> checkLicensePlate(@RequestParam String licensePlate,
                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        boolean exists = vehicleService.checkLicensePlateExists(licensePlate, userPrincipal.getUserId());
        return Result.success(exists);
    }
}