package com.smart_watering_system.SmartWateringSystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.smart_watering_system.SmartWateringSystem.entity.DataSensorHistory;
import com.smart_watering_system.SmartWateringSystem.entity.Device;
import com.smart_watering_system.SmartWateringSystem.mapper.DataSensorMapper;
import com.smart_watering_system.SmartWateringSystem.repository.DataSensorHistoryRepository;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RealtimeService {

    SimpMessagingTemplate simpMessagingTemplate;
    DeviceRepository deviceRepository;
    DataSensorHistoryRepository dataSensorHistoryRepository;
    DataSensorMapper dataSensorMapper;
    ObjectMapper objectMapper;

    @Async
    public void sendDataAsync(String topic, String payload) {
        String[] spliter = topic.split("/");

        String deviceId = spliter[1];
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElse(null);

        if (device == null) return;

        synchronized (deviceId.intern()) {
            try {

                LocalDateTime now = LocalDateTime.now();
                DataSensorHistory dataSensorHistory = objectMapper.readValue(payload, DataSensorHistory.class);
                dataSensorHistory.setTimestamp(now);
                dataSensorHistory.setDevice(device);

                String message = objectMapper.writeValueAsString(dataSensorMapper.toDataSensorResponse(dataSensorHistory));
                log.info("Topic: {}, Data: {}", topic, message);
                simpMessagingTemplate.convertAndSend("/device/" + topic, message);

                Optional<DataSensorHistory> latest =
                        dataSensorHistoryRepository.findTopByDeviceOrderByTimestampDesc(device);
                LocalDateTime preTime = latest.map(DataSensorHistory::getTimestamp).orElse(null);

                if (preTime == null || ChronoUnit.HOURS.between(preTime, now) >= 2)
                    dataSensorHistoryRepository.save(dataSensorHistory);
            } catch (JsonProcessingException | MessagingException e) {
                log.error("sendDataAsync/RealtimeService: {}", e.getMessage());
            }
        }
    }

    public void sendPumpStatus(String topic, String payload) {
        simpMessagingTemplate.convertAndSend("/device/" + topic, payload);
    }

    @Async
    public void sendDeviceStatusAsync(String topic, String payload) {
        simpMessagingTemplate.convertAndSend("/device/" + topic, payload);

        String[] spliter = topic.split("/");
        String deviceId = spliter[1];
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElse(null);

        if (device == null) return;

        synchronized (deviceId.intern()){
            try {
                JsonNode json = objectMapper.readTree(payload);
                boolean isOnline = json.get("isOnline").asBoolean();
                if (device.isOnline() != isOnline) {
                    device.setOnline(isOnline);
                    deviceRepository.save(device);
                }
            } catch (JsonProcessingException e) {
                log.error("sendDeviceStatusAsync/RealtimeService: {}", e.getMessage());
            }
        }
    }

}
