package com.smart_watering_system.SmartWateringSystem.service;

import com.smart_watering_system.SmartWateringSystem.dto.request.ScheduleRequest;
import com.smart_watering_system.SmartWateringSystem.dto.request.TriggerRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ScheduleResponse;
import com.smart_watering_system.SmartWateringSystem.entity.DeviceSchedule;
import com.smart_watering_system.SmartWateringSystem.enums.Day;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.enums.Repeat;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import com.smart_watering_system.SmartWateringSystem.mapper.ScheduleMapper;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeviceSchedulerService {

    DeviceScheduleRepository deviceScheduleRepository;
    DeviceService deviceService;
    ScheduleMapper scheduleMapper;
    TaskScheduler taskScheduler;
    DeviceWateringService deviceWateringService;
    Map<String, ScheduledFuture<?>> schedules;

    public ScheduleResponse create(String id, ScheduleRequest request) {
        if(request.getRepeatType() == Repeat.DAYS) {
            List<Day> days = request.getDaysOfWeek();
            if(Objects.isNull(days) || days.isEmpty()) throw new AppException(ErrorCode.DAYS_OF_WEEK_EMPTY);
        }

        var device = deviceService.getById(id);

        var schedule = scheduleMapper.toDeviceSchedule(request);
        schedule.setDevice(device);
        schedule = deviceScheduleRepository.save(schedule);

        runSchedule(schedule);

        return scheduleMapper.toScheduleResponse(schedule);
    }

    public void runSchedule(DeviceSchedule schedule) {
        if(!schedule.isStatus()) return;

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
                cronExpression = String.format("0 %d %d * * *", startTime.getMinute(), startTime.getHour());

                var scheduler = taskScheduler.schedule(() ->
                        {
                            deviceWateringService.runByScheduler(schedule.getDevice().getId(), schedule.getDuration(), false);
                            turnOffSchedule(schedule);
                        },
                        new CronTrigger(cronExpression));

                schedules.put(schedule.getId(), scheduler);

                return;
            }
        }

        var scheduler = taskScheduler.schedule(() ->
                deviceWateringService.runByScheduler(schedule.getDevice().getId(), schedule.getDuration(), false),
                new CronTrigger(cronExpression));

        schedules.put(schedule.getId(), scheduler);
    }

    public void turnOffSchedule(DeviceSchedule schedule) {
        if(!schedule.isStatus()) return;

        schedules.get(schedule.getId()).cancel(true);
        schedule.setStatus(false);
        deviceScheduleRepository.save(schedule);
    }

    public void turnOnSchedule(DeviceSchedule schedule) {
        if(schedule.isStatus()) return;

        schedules.get(schedule.getId()).cancel(true);

        schedule.setStatus(true);
        schedule = deviceScheduleRepository.save(schedule);
        runSchedule(schedule);
    }

    public void deleteSchedule(String id, String scheduleId) {
        var device = deviceService.getById(id);
        var schedule = deviceScheduleRepository.findByIdAndDevice(scheduleId, device)
                        .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_EXISTED));

        schedules.get(schedule.getId()).cancel(true);
        schedules.remove(schedule.getId());
        deviceScheduleRepository.delete(schedule);
    }

    public ScheduleResponse update(String id, String scheduleId, ScheduleRequest request) {
        if(request.getRepeatType() == Repeat.DAYS) {
            List<Day> days = request.getDaysOfWeek();
            if(Objects.isNull(days) || days.isEmpty()) throw new AppException(ErrorCode.DAYS_OF_WEEK_EMPTY);
        }

        var device = deviceService.getById(id);
        var schedule = deviceScheduleRepository.findByIdAndDevice(scheduleId, device)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_EXISTED));

        scheduleMapper.updateSchedule(schedule, request);
        schedule = deviceScheduleRepository.save(schedule);

        if(schedule.isStatus()) {
            schedules.get(schedule.getId()).cancel(true);
            runSchedule(schedule);
        }

        return scheduleMapper.toScheduleResponse(schedule);
    }

    public void trigger(String id, String scheduleId, TriggerRequest request) {
        var device = deviceService.getById(id);
        var schedule = deviceScheduleRepository.findByIdAndDevice(scheduleId, device)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_EXISTED));

        if (request.isStatus() && schedule.isStatus()) turnOffSchedule(schedule);
        else if(!request.isStatus() && !schedule.isStatus()) turnOnSchedule(schedule);
    }

    public List<ScheduleResponse> getAll(String id, Pageable pageable) {
        var device = deviceService.getById(id);

        List<DeviceSchedule> schedules = deviceScheduleRepository.findAllByDevice(device, pageable);
        return schedules.stream().map(scheduleMapper::toScheduleResponse).toList();
    }

    public ScheduleResponse get(String id, String scheduleId) {
        var device = deviceService.getById(id);
        var schedule = deviceScheduleRepository.findByIdAndDevice(scheduleId, device)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_EXISTED));
        return scheduleMapper.toScheduleResponse(schedule);
    }

}
