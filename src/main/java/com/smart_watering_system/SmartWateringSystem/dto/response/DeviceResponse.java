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
    String topicSensor;
    String topicWatering;
    boolean isOnline;
    boolean isWatering;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    ScheduleResponse nextSchedule;
}
