package com.smart_watering_system.SmartWateringSystem.service;

import com.smart_watering_system.SmartWateringSystem.dto.request.GroupRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.GroupDetailResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.GroupResponse;
import com.smart_watering_system.SmartWateringSystem.entity.User;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import com.smart_watering_system.SmartWateringSystem.mapper.DeviceMapper;
import com.smart_watering_system.SmartWateringSystem.mapper.GroupMapper;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceRepository;
import com.smart_watering_system.SmartWateringSystem.repository.GroupRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupService {

    GroupRepository groupRepository;
    GroupMapper groupMapper;
    DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    public GroupDetailResponse create(GroupRequest request, User user) {
        var group = groupMapper.toGroup(request);
        var devices = request.getDevices().stream()
                .map(id -> deviceRepository.findByIdAndUser(id, user)
                        .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_EXISTED))).toList();
        group.setDevices(devices);
        group.setUser(user);

        try {
            group = groupRepository.save(group);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.GROUP_EXISTED);
        }

        var finalGroup = group;
        devices.forEach(device -> {
            device.setGroup(finalGroup);
            deviceRepository.save(device);
        });

        var groupDetailResponse =  groupMapper.toGroupDetailResponse(group);
        groupDetailResponse.setDevices(devices.stream().map(device -> deviceMapper.toDeviceResponse(device)).toList());

        return groupDetailResponse;
    }

    public List<GroupResponse> getAll(User user) {
        var groups = groupRepository.findAllByUser(user);
        return groups.stream().map(groupMapper::toGroupResponse).toList();
    }

    public GroupDetailResponse get(String id, User user) {
        var group = groupRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_EXISTED));

        var devices = group.getDevices().stream().map(deviceMapper::toDeviceResponse).toList();

        var groupDetailResponse = groupMapper.toGroupDetailResponse(group);
        groupDetailResponse.setDevices(devices);

        return groupDetailResponse;
    }

}
