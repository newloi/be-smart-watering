package com.smart_watering_system.SmartWateringSystem.mapper;

import com.smart_watering_system.SmartWateringSystem.dto.response.ScheduleResponse;
import com.smart_watering_system.SmartWateringSystem.entity.Device;
import com.smart_watering_system.SmartWateringSystem.entity.Group;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceScheduleRepository;
import com.smart_watering_system.SmartWateringSystem.repository.GroupScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MapperHelper {

    DeviceScheduleRepository deviceScheduleRepository;
    GroupScheduleRepository groupScheduleRepository;
    ScheduleMapper scheduleMapper;

    @Named("getNextSchedule")
    ScheduleResponse getNextSchedule(Device device) {
        var nextSchedule = deviceScheduleRepository
                .findFirstByDeviceAndStatusIsTrueAndRunAtAfterOrderByRunAtAsc(device, LocalDateTime.now())
                .orElse(null);
        var scheduleResponse = scheduleMapper
                .toScheduleResponse(nextSchedule);
        if (!Objects.isNull(nextSchedule))
            scheduleResponse.setRunAfter(
                    Duration.between(LocalDateTime.now(), nextSchedule.getRunAt()).getSeconds());

        return scheduleResponse;
    }

    @Named("getNextSchedule")
    ScheduleResponse getNextSchedule(Group group) {
        var nextSchedule = groupScheduleRepository
                .findFirstByGroupAndStatusIsTrueAndRunAtAfterOrderByRunAtAsc(group, LocalDateTime.now())
                .orElse(null);
        var scheduleResponse = scheduleMapper
                .toScheduleResponse(nextSchedule);
        if (!Objects.isNull(nextSchedule))
            scheduleResponse.setRunAfter(
                    Duration.between(LocalDateTime.now(), nextSchedule.getRunAt()).getSeconds());

        return scheduleResponse;
    }

}
