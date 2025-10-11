package com.smart_watering_system.SmartWateringSystem.mapper;

import com.smart_watering_system.SmartWateringSystem.dto.response.DataSensorResponse;
import com.smart_watering_system.SmartWateringSystem.entity.DataSensorHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DataSensorMapper {
    DataSensorResponse toDataSensorResponse(DataSensorHistory dataSensorHistory);
}
