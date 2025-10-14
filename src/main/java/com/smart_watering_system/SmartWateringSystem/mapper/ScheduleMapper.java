package com.smart_watering_system.SmartWateringSystem.mapper;

import com.smart_watering_system.SmartWateringSystem.dto.request.ScheduleRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ScheduleResponse;
import com.smart_watering_system.SmartWateringSystem.entity.DeviceSchedule;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {
    DeviceSchedule toDeviceSchedule(ScheduleRequest request);
    ScheduleResponse toScheduleResponse(DeviceSchedule schedule);
}
