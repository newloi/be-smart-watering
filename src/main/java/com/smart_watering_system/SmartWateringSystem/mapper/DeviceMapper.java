package com.smart_watering_system.SmartWateringSystem.mapper;

import com.smart_watering_system.SmartWateringSystem.dto.request.DeviceRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.DeviceResponse;
import com.smart_watering_system.SmartWateringSystem.entity.Device;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = MapperHelper.class)
public interface DeviceMapper {
    Device toDevice(DeviceRequest request);
    @Mapping(target = "isOnline", source = "online")
    @Mapping(target = "isWatering", source = "watering")
    @Mapping(target = "nextSchedule", source = "device", qualifiedByName = "getNextSchedule")
    DeviceResponse toDeviceResponse(Device device);
    void updateDevice(@MappingTarget Device device, DeviceRequest request);
}
