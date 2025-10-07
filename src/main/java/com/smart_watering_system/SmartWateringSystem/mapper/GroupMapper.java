package com.smart_watering_system.SmartWateringSystem.mapper;

import com.smart_watering_system.SmartWateringSystem.dto.request.GroupRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.GroupDetailResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.GroupResponse;
import com.smart_watering_system.SmartWateringSystem.entity.Group;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    @Mapping(target = "devices", ignore = true)
    Group toGroup(GroupRequest request);

    @Mapping(target = "devices", ignore = true)
    GroupDetailResponse toGroupDetailResponse(Group group);

    GroupResponse toGroupResponse(Group group);

    @Mapping(target = "devices", ignore = true)
    void updateGroup(@MappingTarget Group group, GroupRequest request);

}
