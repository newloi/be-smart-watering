package com.smart_watering_system.SmartWateringSystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smart_watering_system.SmartWateringSystem.dto.response.DataSensorResponse;
import com.smart_watering_system.SmartWateringSystem.entity.DataSensorHistory;
import com.smart_watering_system.SmartWateringSystem.entity.Device;
import com.smart_watering_system.SmartWateringSystem.entity.DeviceWateringHistory;
import com.smart_watering_system.SmartWateringSystem.entity.User;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import com.smart_watering_system.SmartWateringSystem.mapper.DataSensorMapper;
import com.smart_watering_system.SmartWateringSystem.repository.DataSensorHistoryRepository;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SensorService {

    SimpMessagingTemplate simpMessagingTemplate;
    Executor executor;
    DeviceRepository deviceRepository;
    DataSensorHistoryRepository dataSensorHistoryRepository;
    DataSensorMapper dataSensorMapper;

    public void sendData(String topic, String payload) throws JsonProcessingException {
        String[] spliter = topic.split("/");
        String deviceId = spliter[1];
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        DataSensorHistory dataSensorHistory = objectMapper.readValue(payload, DataSensorHistory.class);
        dataSensorHistory.setTimestamp(now);
        dataSensorHistory.setDevice(device);

        String message = objectMapper.writeValueAsString(dataSensorMapper.toDataSensorResponse(dataSensorHistory));
        log.info("Topic: " + topic + ", Data: " + message);
        simpMessagingTemplate.convertAndSend("/device/" + topic, message);

        if (device != null) {
            executor.execute(() -> {
                Optional<DataSensorHistory> latest =
                        dataSensorHistoryRepository.findTopByDeviceOrderByTimestampDesc(device);
                LocalDateTime preTime = latest.map(DataSensorHistory::getTimestamp).orElse(null);

                if (preTime == null || ChronoUnit.HOURS.between(preTime, now) >= 2)
                    dataSensorHistoryRepository.save(dataSensorHistory);

            });
        }
    }

    public List<DataSensorResponse> getHistory(String id, User user, Pageable pageable) {
        Device device = deviceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_EXISTED));

        List<DataSensorHistory> histories = dataSensorHistoryRepository.findAllByDeviceOrderByTimestampDesc(device, pageable);
        return histories.stream().map(dataSensorMapper::toDataSensorResponse).toList();
    }

}
