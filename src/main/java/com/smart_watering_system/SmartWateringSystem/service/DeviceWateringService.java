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
    public WateringResponse doAction(String id, WateringRequest request, User user, boolean byGroup) throws MqttException, JsonProcessingException {
        Device device = deviceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_EXISTED));

        String action = request.getAction();
        DeviceWateringHistory recentWatering = Optional.ofNullable(device.getHistories())
                .filter(list -> !list.isEmpty())
                .map(List::getFirst)
                .orElse(null);
        boolean isRunning = !Objects.isNull(recentWatering) ? LocalDateTime.now().isBefore(recentWatering.getStartTime().plusSeconds(recentWatering.getDuration())) : false;

        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(request);

        if (Objects.equals(action, Action.START.name())) {
            if(!isRunning) {

                mqttSevice.publishAsync(device.getTopicWatering(), message);

                DeviceWateringHistory history = wateringMapper.toDeviceWateringHistory(request);
                history.setDevice(device);
                history.setByGroup(byGroup);
                history = deviceWateringHistoryRepository.save(history);

                var response = wateringMapper.toWateringResponse(history);
                response.setAction(action);

                return response;
            }

        } else if (Objects.equals(action, Action.STOP.name())) {
            if(isRunning) {

                mqttSevice.publishAsync(device.getTopicWatering(), message);

                recentWatering.setDuration(ChronoUnit.SECONDS.between(recentWatering.getStartTime(), LocalDateTime.now()));
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

        List<DeviceWateringHistory> histories = deviceWateringHistoryRepository.findAllByDevice(device, Pageable.ofSize(10));
        return histories.stream().map(wateringMapper::toWateringResponse).toList();
    }

}
