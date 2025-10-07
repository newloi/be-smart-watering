package com.smart_watering_system.SmartWateringSystem.service;

import com.smart_watering_system.SmartWateringSystem.dto.request.DeviceRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.DeviceResponse;
import com.smart_watering_system.SmartWateringSystem.entity.User;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import com.smart_watering_system.SmartWateringSystem.mapper.DeviceMapper;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeviceService {

    DeviceRepository deviceRepository;
    DeviceMapper deviceMapper;

    public DeviceResponse create(DeviceRequest request, User user) {
        var device = deviceMapper.toDevice(request);

        device.setTopicSensor("sensor/" + request.getDeviceId());
        device.setTopicWatering("watering/" + request.getDeviceId());
        device.setWatering(false);
        device.setUser(user);

        try {
            device = deviceRepository.save(device);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.DEVICE_EXISTED);
        }

        return deviceMapper.toDeviceResponse(device);
    }

    public List<DeviceResponse> getAll(User user) {
        return deviceRepository.findAllByUser(user).stream()
                .map(deviceMapper::toDeviceResponse).toList();
    }

    public DeviceResponse get(String id, User user) {
        return deviceMapper.toDeviceResponse(deviceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_EXISTED)));
    }

    @Transactional
    public void delete(String id, User user) {
        deviceRepository.deleteByIdAndUser(id, user);
    }

    public DeviceResponse update(String id, DeviceRequest request, User user) {
        var device = deviceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_EXISTED));

        deviceMapper.updateDevice(device, request);

        return deviceMapper.toDeviceResponse(deviceRepository.save(device));
    }

}
