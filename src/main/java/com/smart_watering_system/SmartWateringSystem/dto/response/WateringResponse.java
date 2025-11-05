package com.smart_watering_system.SmartWateringSystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smart_watering_system.SmartWateringSystem.enums.Action;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WateringResponse {
    Action action;
    long duration;
    boolean byGroup;
    LocalDateTime startTime;
}
