package com.carmaintenance.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;

/**
 * 预约DTO
 */
@Data
public class AppointmentDTO {

    @NotNull(message = "门店ID不能为空")
    private Long shopId;

    @NotNull(message = "车辆ID不能为空")
    private Long vehicleId;

    @NotNull(message = "预约日期不能为空")
    @FutureOrPresent(message = "预约日期不能是过去的日期")
    private LocalDate appointmentDate;

    @NotBlank(message = "预约时间段不能为空")
    private String timeSlot;

    @Min(value = 1, message = "工位号至少为1")
    private Integer bayNumber;

    private List<AppointmentItemDTO> items;

    private String remark;
}