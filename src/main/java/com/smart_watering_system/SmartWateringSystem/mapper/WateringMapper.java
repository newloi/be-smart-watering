package com.smart_watering_system.SmartWateringSystem.mapper;

import com.smart_watering_system.SmartWateringSystem.dto.request.WateringRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.WateringResponse;
import com.smart_watering_system.SmartWateringSystem.entity.DeviceWateringHistory;
import com.smart_watering_system.SmartWateringSystem.entity.GroupWateringHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WateringMapper{

    WateringResponse toWateringResponse(DeviceWateringHistory history);

    @Mapping(target = "byGroup", constant = "true")
    WateringResponse toWateringResponse(GroupWateringHistory history);

    DeviceWateringHistory toDeviceWateringHistory(WateringRequest request);

    GroupWateringHistory toGroupWateringHistory(WateringRequest request);

}
