package com.smart_watering_system.SmartWateringSystem.service;

import com.smart_watering_system.SmartWateringSystem.dto.request.DeviceRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.DataSensorResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.DeviceResponse;
import com.smart_watering_system.SmartWateringSystem.entity.DataSensorHistory;
import com.smart_watering_system.SmartWateringSystem.entity.Device;
import com.smart_watering_system.SmartWateringSystem.entity.User;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import com.smart_watering_system.SmartWateringSystem.mapper.DataSensorMapper;
import com.smart_watering_system.SmartWateringSystem.mapper.DeviceMapper;
import com.smart_watering_system.SmartWateringSystem.repository.DataSensorHistoryRepository;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeviceService {

    DeviceRepository deviceRepository;
    DeviceMapper deviceMapper;
    MqttSevice mqttSevice;
    UserService userService;
    DataSensorHistoryRepository dataSensorHistoryRepository;
    DataSensorMapper dataSensorMapper;
    RealtimeService realtimeService;

    public DeviceResponse create(DeviceRequest request) {
        var device = deviceMapper.toDevice(request);

        device.setTopicSensor("sensor/" + request.getDeviceId());
        device.setTopicWatering("watering/" + request.getDeviceId());
        device.setOnline(true);
        device.setUser(userService.getUser());

        try {
            device = deviceRepository.save(device);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.DEVICE_EXISTED);
        }

        var response = deviceMapper.toDeviceResponse(device);
        return response;
    }

    public List<DeviceResponse> getAll(Pageable pageable) {
        return deviceRepository.findAllByUser(userService.getUser(), pageable).stream()
                .map(deviceMapper::toDeviceResponse).toList();
    }

    @Cacheable(value = "devices", key = "#id + '-' + #user.id")
    public DeviceResponse get(String id, User user) throws MqttException {
        var device = deviceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_EXISTED));

        mqttSevice.subcribeAsync(device.getTopicWatering(),
                (topic, message) -> realtimeService.sendPumpStatus(topic, message.toString()));

        return deviceMapper.toDeviceResponse(device);
    }

    @CacheEvict(value = "devices", key = "#id + '-' + #user.id")
    public void delete(String id, User user) throws MqttException {
        var device = deviceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_EXISTED));

        mqttSevice.unsubscribeAsync(
                new String[]{device.getTopicSensor(), device.getTopicWatering(), "status/" + device.getDeviceId()}
        );

        deviceRepository.delete(device);
    }

    @CachePut(value = "devices", key = "#id + '-' + #user.id")
    public DeviceResponse update(String id, DeviceRequest request, User user) throws MqttException {
        var device = deviceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_EXISTED));

        if(!Objects.equals(device.getDeviceId(), request.getDeviceId())) {
            mqttSevice.unsubscribeAsync(
                    new String[]{device.getTopicSensor(), device.getTopicWatering(), "status/" + device.getDeviceId()}
            );

            deviceMapper.updateDevice(device, request);
            device.setTopicSensor("sensor/" + request.getDeviceId());
            device.setTopicWatering("watering/" + request.getDeviceId());
        }

        return deviceMapper.toDeviceResponse(deviceRepository.save(device));
    }

    public List<DeviceResponse> getAllFree(Pageable pageable) {
        return deviceRepository.findByGroupIsNullAndUser(userService.getUser(), pageable)
                .stream().map(deviceMapper::toDeviceResponse).toList();
    }

    public Device getById(String id) {
        return deviceRepository.findByIdAndUser(id, userService.getUser())
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_EXISTED));
    }

    public List<DeviceResponse> searchByKeyword(String keyword, Pageable pageable) {
        return deviceRepository.findByUserAndNameContainingIgnoreCase(userService.getUser(), keyword, pageable)
                .stream().map(deviceMapper::toDeviceResponse).toList();
    }

    public List<DataSensorResponse> getHistorySensor(String id,Pageable pageable) {
        Device device = deviceRepository.findByIdAndUser(id, userService.getUser())
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_EXISTED));

        List<DataSensorHistory> histories = dataSensorHistoryRepository.findAllByDevice(device, pageable);
        return histories.stream().map(dataSensorMapper::toDataSensorResponse).toList();
    }

}
