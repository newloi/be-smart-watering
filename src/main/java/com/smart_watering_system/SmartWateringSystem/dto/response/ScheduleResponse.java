package com.smart_watering_system.SmartWateringSystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smart_watering_system.SmartWateringSystem.enums.Day;
import com.smart_watering_system.SmartWateringSystem.enums.Repeat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleResponse {
    String id;
    LocalTime startTime;
    Date dateOneTime;
    long duration;
    Repeat repeatType;
    List<Day> daysOfWeek;
}
