package com.smart_watering_system.SmartWateringSystem.mapper;

import com.smart_watering_system.SmartWateringSystem.dto.request.ScheduleRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ScheduleResponse;
import com.smart_watering_system.SmartWateringSystem.entity.DeviceSchedule;
import com.smart_watering_system.SmartWateringSystem.entity.GroupSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {
    @Mapping(target = "status", constant = "true")
    DeviceSchedule toDeviceSchedule(ScheduleRequest request);
    ScheduleResponse toScheduleResponse(DeviceSchedule schedule);
    void updateSchedule(@MappingTarget DeviceSchedule schedule, ScheduleRequest request);

    @Mapping(target = "status", constant = "true")
    GroupSchedule toGroupSchedule(ScheduleRequest request);
    ScheduleResponse toScheduleResponse(GroupSchedule schedule);
    void updateSchedule(@MappingTarget GroupSchedule schedule, ScheduleRequest request);
}
