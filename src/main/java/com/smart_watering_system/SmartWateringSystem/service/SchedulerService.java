package com.smart_watering_system.SmartWateringSystem.service;

import com.smart_watering_system.SmartWateringSystem.dto.request.ScheduleRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ScheduleResponse;
import com.smart_watering_system.SmartWateringSystem.entity.DeviceSchedule;
import com.smart_watering_system.SmartWateringSystem.enums.Day;
import com.smart_watering_system.SmartWateringSystem.enums.Repeat;
import com.smart_watering_system.SmartWateringSystem.mapper.ScheduleMapper;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Date;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SchedulerService {

    DeviceScheduleRepository deviceScheduleRepository;
    DeviceService deviceService;
    ScheduleMapper scheduleMapper;
    TaskScheduler taskScheduler;
    DeviceWateringService deviceWateringService;

    public ScheduleResponse create(String id, ScheduleRequest request, String authHeader) {
        var device = deviceService.getByUser(id, authHeader);

        var schedule = scheduleMapper.toDeviceSchedule(request);
        schedule.setDevice(device);
        schedule = deviceScheduleRepository.save(schedule);

        runSchedule(schedule);

        return scheduleMapper.toScheduleResponse(schedule);
    }

    public void runSchedule(DeviceSchedule schedule) {
        String cronExpression = null;

        Repeat repeatType = schedule.getRepeatType();
        LocalTime startTime = schedule.getStartTime();
        switch (repeatType) {
            case EVERYDAY -> {
                cronExpression = String.format("0 %d %d * * *", startTime.getMinute(), startTime.getHour());
            }
            case DAYS -> {
                String days = schedule.getDaysOfWeek().stream().map(Day::name).collect(Collectors.joining(","));
                cronExpression = String.format("0 %d %d * * %s", startTime.getMinute(), startTime.getHour(), days);
            }
            case ONE_TIME -> {
                if(schedule.getDateOneTime().after(new Date())) {
                    taskScheduler.schedule(() ->
                                    deviceWateringService.runStartByScheduler(schedule.getDevice().getId(), schedule.getDuration()),
                            schedule.getDateOneTime());
                }
                return;
            }
        }

        taskScheduler.schedule(() ->
                deviceWateringService.runStartByScheduler(schedule.getDevice().getId(), schedule.getDuration()),
                new CronTrigger(cronExpression));
    }

}
