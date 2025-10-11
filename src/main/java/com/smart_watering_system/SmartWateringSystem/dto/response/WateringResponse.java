package com.smart_watering_system.SmartWateringSystem.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WateringResponse {
    String action;
    long duration;
    boolean byGroup;
    LocalDateTime startTime;
}
