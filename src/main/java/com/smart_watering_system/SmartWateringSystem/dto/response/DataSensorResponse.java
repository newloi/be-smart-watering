package com.smart_watering_system.SmartWateringSystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataSensorResponse {
    String deviceId;
    float temp;
    float air;
    float soil;
    LocalDateTime timestamp;
}
