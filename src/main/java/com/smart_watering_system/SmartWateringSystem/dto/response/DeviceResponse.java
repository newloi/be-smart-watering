package com.smart_watering_system.SmartWateringSystem.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeviceResponse {
    String id;
    String deviceId;
    String name;
    boolean isOnline;
    String topicSensor;
    String topicWatering;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
