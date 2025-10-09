package com.smart_watering_system.SmartWateringSystem.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SensorService {

    SimpMessagingTemplate simpMessagingTemplate;

    public void sendDataSensor(String topic, String payload) {
        String deviceId = topic.substring(7);

        simpMessagingTemplate.convertAndSend("/device/" + deviceId, payload);

        log.info("DeviceId: " + deviceId + ", Data: " + payload);
    }

}
