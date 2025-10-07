package com.smart_watering_system.SmartWateringSystem.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
}
