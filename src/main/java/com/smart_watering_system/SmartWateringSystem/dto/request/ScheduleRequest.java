package com.smart_watering_system.SmartWateringSystem.dto.request;

import com.smart_watering_system.SmartWateringSystem.enums.Day;
import com.smart_watering_system.SmartWateringSystem.enums.Repeat;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScheduleRequest {
    @NotBlank(message = "NOT_BLANK")
    LocalTime startTime;

    @NotBlank(message = "NOT_BLANK")
    long duration;

    @NotBlank(message = "NOT_BLANK")
    Repeat repeatType;

    List<Day> daysOfWeek;
    boolean status;
}
