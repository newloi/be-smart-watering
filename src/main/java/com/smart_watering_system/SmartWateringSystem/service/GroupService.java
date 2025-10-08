package com.smart_watering_system.SmartWateringSystem.service;

import com.smart_watering_system.SmartWateringSystem.dto.request.GroupRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.GroupDetailResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.GroupResponse;
import com.smart_watering_system.SmartWateringSystem.entity.Device;
import com.smart_watering_system.SmartWateringSystem.entity.User;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import com.smart_watering_system.SmartWateringSystem.mapper.DeviceMapper;
import com.smart_watering_system.SmartWateringSystem.mapper.GroupMapper;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceRepository;
import com.smart_watering_system.SmartWateringSystem.repository.GroupRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        group.setWatering(false);

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

        var groupDetailResponse = groupMapper.toGroupDetailResponse(group);
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

    public GroupDetailResponse update(String id, GroupRequest request, User user) {
        var group = groupRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_EXISTED));

        group.setName(request.getName());

        List<Device> newDevices = request.getDevices().stream().map(device -> deviceRepository.findByIdAndUser(device, user)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_EXISTED))).toList();

        var tmpGroup = group;
        var oldDevices = group.getDevices();
        oldDevices.removeIf(device -> {
            if (!newDevices.contains(device)) {
                device.setGroup(null);
                return true;
            }
            return false;
        });
        newDevices.forEach(device -> {
            if (!oldDevices.contains(device)) {
                oldDevices.add(device);
                device.setGroup(tmpGroup);
            }
        });
        group = groupRepository.save(group);

        var devices = group.getDevices().stream().map(deviceMapper::toDeviceResponse).toList();

        var groupDetailResponse = groupMapper.toGroupDetailResponse(group);
        groupDetailResponse.setDevices(devices);

        return groupDetailResponse;
    }

    @Transactional
    public void delete(String id, User user) {
        groupRepository.deleteByIdAndUser(id, user);
    }

}
