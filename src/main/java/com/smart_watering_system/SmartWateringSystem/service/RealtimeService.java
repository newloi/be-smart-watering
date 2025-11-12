package com.smart_watering_system.SmartWateringSystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smart_watering_system.SmartWateringSystem.dto.request.WateringRequest;
import com.smart_watering_system.SmartWateringSystem.entity.DataSensorHistory;
import com.smart_watering_system.SmartWateringSystem.entity.Device;
import com.smart_watering_system.SmartWateringSystem.entity.Group;
import com.smart_watering_system.SmartWateringSystem.enums.Action;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import com.smart_watering_system.SmartWateringSystem.mapper.DataSensorMapper;
import com.smart_watering_system.SmartWateringSystem.repository.DataSensorHistoryRepository;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceRepository;
import com.smart_watering_system.SmartWateringSystem.repository.GroupRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
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
    DeviceWateringService deviceWateringService;
    Executor taskExecutor;
    GroupRepository groupRepository;

    @Transactional
    public void sendData(String topic, String payload) {
        log.info("Received: {}", payload);
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

                var response = dataSensorMapper.toDataSensorResponse(dataSensorHistory);
                response.setDeviceId(deviceId);

                sendMessageTo(
                        device.getUser().getUsername(),
                        objectMapper.writeValueAsString(response),
                        "/devices/sensor", "/device/" + device.getTopicSensor());



                Optional<DataSensorHistory> latest =
                        dataSensorHistoryRepository.findTopByDeviceOrderByTimestampDesc(device);
                LocalDateTime preTime = latest.map(DataSensorHistory::getTimestamp).orElse(null);

                if (preTime == null || ChronoUnit.HOURS.between(preTime, now) >= 2) {
                    dataSensorHistoryRepository.save(dataSensorHistory);
                    log.info("saved: {}", payload);
                }
            } catch (JsonProcessingException | MessagingException e) {
                log.error("RealtimeService.sendData: {}", e.getMessage());
            }
        }
    }

    @Transactional
    public void sendPumpStatus(String topic, String payload) {
        String[] spliter = topic.split("/");
        String deviceId = spliter[2];
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElse(null);

        if (device == null) return;

        synchronized (deviceId.intern()) {
            try {
                JsonNode json = objectMapper.readTree(payload);

                boolean isWatering = json.get("isWatering").asBoolean();
                if (device.isWatering() != isWatering) {
                    device.setWatering(isWatering);
                    deviceRepository.save(device);
                }

                if (json instanceof ObjectNode objectNode) {
                    objectNode.put("deviceId", deviceId);
                }
                sendMessageTo(
                        device.getUser().getUsername(),
                        objectMapper.writeValueAsString(json),
                        "/devices/watering", "/device/" + device.getTopicWatering());
            } catch (JsonProcessingException e) {
                log.error("RealtimeService.sendPumpStatus: {}", e.getMessage());
            }
        }
    }

    @Transactional
    public void sendDeviceStatus(String topic, String payload) {
        String[] spliter = topic.split("/");
        String deviceId = spliter[1];
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElse(null);

        if (device == null) return;

        synchronized (deviceId.intern()) {
            try {
                JsonNode json = objectMapper.readTree(payload);

                boolean isOnline = json.get("isOnline").asBoolean();
                if (device.isOnline() != isOnline) {
                    device.setOnline(isOnline);
                    if(!isOnline && device.isWatering()) {
                        Device finalDevice = device;
                        taskExecutor.execute(() -> {
                            WateringRequest request = WateringRequest.builder()
                                    .action(Action.STOP)
                                    .duration(0)
                                    .build();
                            try {
                                deviceWateringService.doAction(finalDevice.getId(), request, false, finalDevice.getUser());
                            } catch (MqttException | JsonProcessingException e) {
                                log.error("RealtimeService.sendDeviceStatus.taskExecutor: {}", e.getMessage());
                            }
                            finalDevice.setWatering(false);
                            deviceRepository.save(finalDevice);
                        });
                    }
                    device = deviceRepository.save(device);
                }

                if (json instanceof ObjectNode objectNode) {
                    objectNode.put("deviceId", deviceId);
                }
                sendMessageTo(
                        device.getUser().getUsername(),
                        objectMapper.writeValueAsString(json),
                        "/devices/status", "/device/status/" + deviceId);
            } catch (JsonProcessingException e) {
                log.error("RealtimeService.sendDeviceStatus: {}", e.getMessage());
            }
        }
    }

    public void sendGroupWateringStatus(Group group, boolean isWatering) {
        group.setWatering(isWatering);
        groupRepository.save(group);

        String message = "{\"groupId\":\"" + group.getId() + "\",\"isWatering\":" + isWatering + "}";

        sendMessageTo(group.getUser().getUsername(), message,
                "/groups/watering", "/group/watering/" + group.getId());
    }

    @Async
    public void sendMessageTo(String user, String message, String... destinations) {
        for(var des : destinations) {
            simpMessagingTemplate.convertAndSendToUser(user, des, message);
            log.info("Sent: {}", message);
        }
    }

}
