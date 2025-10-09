package com.smart_watering_system.SmartWateringSystem.service;

import com.smart_watering_system.SmartWateringSystem.dto.request.DeviceRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.DeviceResponse;
import com.smart_watering_system.SmartWateringSystem.entity.Device;
import com.smart_watering_system.SmartWateringSystem.entity.User;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import com.smart_watering_system.SmartWateringSystem.mapper.DeviceMapper;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeviceService {

    DeviceRepository deviceRepository;
    DeviceMapper deviceMapper;
    MqttClient mqttClient;
    SensorService sensorService;

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

    public DeviceResponse get(String id, User user) throws MqttException {
        var device = deviceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_EXISTED));

        mqttClient.subscribe(device.getTopicSensor(), (topic, payload) -> {
            String message = new String(payload.getPayload(), StandardCharsets.UTF_8);
            sensorService.sendDataSensor(topic, message);
        });

        return deviceMapper.toDeviceResponse(device);
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

    public List<DeviceResponse> getAllFree(User user) {
        return deviceRepository.findByGroupIsNullAndUser(user).stream().map(deviceMapper::toDeviceResponse).toList();
    }

}
