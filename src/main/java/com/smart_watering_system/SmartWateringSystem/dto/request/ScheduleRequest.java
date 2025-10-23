package com.smart_watering_system.SmartWateringSystem.dto.request;

import com.smart_watering_system.SmartWateringSystem.enums.Day;
import com.smart_watering_system.SmartWateringSystem.enums.Repeat;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleRequest {
    @NotNull(message = "NOT_BLANK")
    LocalTime startTime;

    @NotNull(message = "NOT_BLANK")
    long duration;

    @NotNull(message = "NOT_BLANK")
    Repeat repeatType;

    List<Day> daysOfWeek;
}
