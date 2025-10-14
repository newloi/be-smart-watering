package com.smart_watering_system.SmartWateringSystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart_watering_system.SmartWateringSystem.dto.request.WateringRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.WateringResponse;
import com.smart_watering_system.SmartWateringSystem.entity.Device;
import com.smart_watering_system.SmartWateringSystem.entity.DeviceWateringHistory;
import com.smart_watering_system.SmartWateringSystem.entity.User;
import com.smart_watering_system.SmartWateringSystem.enums.Action;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import com.smart_watering_system.SmartWateringSystem.mapper.WateringMapper;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceRepository;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceWateringHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeviceWateringService {

    DeviceWateringHistoryRepository deviceWateringHistoryRepository;
    MqttSevice mqttSevice;
    WateringMapper wateringMapper;
    DeviceRepository deviceRepository;

    @Transactional
    public WateringResponse doAction(String id, WateringRequest request, User user, boolean byGroup)
            throws MqttException, JsonProcessingException {
        Device device = deviceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_EXISTED));

        Action action = request.getAction();
        DeviceWateringHistory recentWatering = device.getHistories().isEmpty() ? null : device.getHistories().getFirst();
        boolean isRunning = !Objects.isNull(recentWatering) && LocalDateTime.now().isBefore(recentWatering.getStartTime()
                .plusSeconds(recentWatering.getDuration()));

        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(request);

        if (action == Action.START) {
            if (!isRunning) {

                mqttSevice.publishAsync(device.getTopicWatering(), message);

                DeviceWateringHistory history = wateringMapper.toDeviceWateringHistory(request);
                history.setDevice(device);
                history.setByGroup(byGroup);
                history = deviceWateringHistoryRepository.save(history);

                var response = wateringMapper.toWateringResponse(history);
                response.setAction(action);

                return response;
            }

        } else if (action == Action.STOP) {
            if (isRunning) {

                mqttSevice.publishAsync(device.getTopicWatering(), message);

                recentWatering.setDuration(
                        ChronoUnit.SECONDS.between(recentWatering.getStartTime(), LocalDateTime.now())
                );
                var history = deviceWateringHistoryRepository.save(recentWatering);

                var response = wateringMapper.toWateringResponse(history);
                response.setAction(action);

                return response;
            }
        } else throw new AppException(ErrorCode.INVALID_ACTION);

        return WateringResponse.builder().build();
    }

    public List<WateringResponse> getAllHistories(String id, User user) {
        Device device = deviceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_EXISTED));

        List<DeviceWateringHistory> histories = deviceWateringHistoryRepository
                .findAllByDeviceOrderByStartTimeDesc(device, Pageable.ofSize(10));
        return histories.stream().map(wateringMapper::toWateringResponse).toList();
    }

    public void runStartByScheduler(String id, long duration) {
        Device device = deviceRepository.findByIdWithHistories(id);
        DeviceWateringHistory recentWatering = device.getHistories().isEmpty() ? null : device.getHistories().getFirst();
        boolean isRunning = !Objects.isNull(recentWatering) && LocalDateTime.now().isBefore(recentWatering.getStartTime()
                .plusSeconds(recentWatering.getDuration()));

        if (!isRunning) {
            WateringRequest request = new WateringRequest(Action.START, duration);

            ObjectMapper objectMapper = new ObjectMapper();
            String message = null;
            try {
                message = objectMapper.writeValueAsString(request);
                mqttSevice.publishAsync(device.getTopicWatering(), message);
            } catch (JsonProcessingException | MqttException ignored) {}

            DeviceWateringHistory history = wateringMapper.toDeviceWateringHistory(request);
            history.setDevice(device);
            history.setByGroup(false);
            deviceWateringHistoryRepository.save(history);
        }
    }

}
