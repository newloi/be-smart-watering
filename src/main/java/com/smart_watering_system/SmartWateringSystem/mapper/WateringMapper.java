package com.smart_watering_system.SmartWateringSystem.mapper;

import com.smart_watering_system.SmartWateringSystem.dto.request.WateringRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.WateringResponse;
import com.smart_watering_system.SmartWateringSystem.entity.DeviceWateringHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface WateringMapper{

    WateringResponse toWateringResponse(DeviceWateringHistory history);

    @Mapping(target = "byGroup", constant = "false")
    @Mapping(target = "startTime", expression = "java(java.time.LocalDateTime.now())")
    DeviceWateringHistory toDeviceWateringHistory(WateringRequest request);

}
