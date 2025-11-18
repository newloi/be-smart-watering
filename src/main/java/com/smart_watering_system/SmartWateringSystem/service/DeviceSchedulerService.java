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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Slf4j
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
        if (request.getRepeatType() == Repeat.DAYS) {
            List<Day> days = request.getDaysOfWeek();
            if (Objects.isNull(days) || days.isEmpty()) throw new AppException(ErrorCode.DAYS_OF_WEEK_EMPTY);
        }

        var device = deviceService.getById(id);

        var schedule = scheduleMapper.toDeviceSchedule(request);
        schedule.setDevice(device);
        schedule = deviceScheduleRepository.save(schedule);

        runSchedule(schedule);

        return scheduleMapper.toScheduleResponse(schedule);
    }

    public void runSchedule(DeviceSchedule schedule) {
        if (!schedule.isStatus()) return;

        String cronExpression = null;

        Repeat repeatType = schedule.getRepeatType();
        LocalTime startTime = schedule.getStartTime();
        LocalTime timeNow = LocalTime.now();
        LocalDate dateNow = LocalDate.now();
        switch (repeatType) {
            case EVERYDAY -> {
                cronExpression = String.format("0 %d %d * * *", startTime.getMinute(), startTime.getHour());
                if (startTime.isBefore(timeNow)) schedule.setRunAt(LocalDateTime.of(dateNow.plusDays(1), startTime));
                else schedule.setRunAt(LocalDateTime.of(dateNow, startTime));
                deviceScheduleRepository.save(schedule);
            }
            case DAYS -> {
                String days = schedule.getDaysOfWeek().stream().map(Day::name).collect(Collectors.joining(","));
                cronExpression = String.format("0 %d %d * * %s", startTime.getMinute(), startTime.getHour(), days);

                int currentDayOfWeek = dateNow.getDayOfWeek().getValue();
                int dayStep = schedule.getDaysOfWeek().stream().mapToInt(day ->
                        (day.ordinal() + 1 - currentDayOfWeek + 7) % 7
                ).min().orElseThrow();
                schedule.setRunAt(LocalDateTime.of(dateNow, startTime).plusDays(dayStep));
                deviceScheduleRepository.save(schedule);
            }
            case ONE_TIME -> {
                cronExpression = String.format("0 %d %d * * *", startTime.getMinute(), startTime.getHour());
                if (startTime.isBefore(timeNow)) schedule.setRunAt(LocalDateTime.of(dateNow.plusDays(1), startTime));
                else schedule.setRunAt(LocalDateTime.of(dateNow, startTime));
                deviceScheduleRepository.save(schedule);

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

        var scheduler = taskScheduler.schedule(() -> {
                    Repeat type = schedule.getRepeatType();
                    deviceWateringService.runByScheduler(schedule.getDevice().getId(), schedule.getDuration(), false);
                    if (type == Repeat.EVERYDAY) {
                        schedule.setRunAt(schedule.getRunAt().plusDays(1));
                        deviceScheduleRepository.save(schedule);
                    } else if (type == Repeat.DAYS) {
                        int currentDayOfWeek = LocalDate.now().getDayOfWeek().getValue();
                        int dayStep = schedule.getDaysOfWeek().stream().mapToInt(day ->
                                (day.ordinal() + 1 - currentDayOfWeek + 6) % 7 + 1
                        ).min().orElseThrow();
                        schedule.setRunAt(schedule.getRunAt().plusDays(dayStep));
                        deviceScheduleRepository.save(schedule);
                    }
                },
                new CronTrigger(cronExpression));

        schedules.put(schedule.getId(), scheduler);
    }

    public void turnOffSchedule(DeviceSchedule schedule) {
        if (!schedule.isStatus()) return;

        schedules.get(schedule.getId()).cancel(true);
        schedules.remove(schedule.getId());
        schedule.setStatus(false);
        deviceScheduleRepository.save(schedule);
    }

    public void turnOnSchedule(DeviceSchedule schedule) {
        if (schedule.isStatus()) return;

        schedule.setStatus(true);
        schedule = deviceScheduleRepository.save(schedule);
        runSchedule(schedule);
    }

    public void deleteSchedule(String id, String scheduleId) {
        var device = deviceService.getById(id);
        var schedule = deviceScheduleRepository.findByIdAndDevice(scheduleId, device)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_EXISTED));

        if (schedules.containsKey(schedule.getId())) {
            schedules.get(schedule.getId()).cancel(true);
            schedules.remove(schedule.getId());
        }
        deviceScheduleRepository.delete(schedule);
    }

    public ScheduleResponse update(String id, String scheduleId, ScheduleRequest request) {
        if (request.getRepeatType() == Repeat.DAYS) {
            List<Day> days = request.getDaysOfWeek();
            if (Objects.isNull(days) || days.isEmpty()) throw new AppException(ErrorCode.DAYS_OF_WEEK_EMPTY);
        }

        var device = deviceService.getById(id);
        var schedule = deviceScheduleRepository.findByIdAndDevice(scheduleId, device)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_EXISTED));

        scheduleMapper.updateSchedule(schedule, request);
        schedule = deviceScheduleRepository.save(schedule);

        if (schedule.isStatus()) {
            schedules.get(schedule.getId()).cancel(true);
            runSchedule(schedule);
        }

        var response = scheduleMapper.toScheduleResponse(schedule);
        if (schedule.isStatus()) {
            response.setRunAfter(Duration.between(LocalDateTime.now(), schedule.getRunAt()).getSeconds());
        } else {
            response.setRunAfter(-1);
        }

        return response;
    }

    public void trigger(String id, String scheduleId, TriggerRequest request) {
        var device = deviceService.getById(id);
        var schedule = deviceScheduleRepository.findByIdAndDevice(scheduleId, device)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_EXISTED));

        if (request.isStatus() && schedule.isStatus()) turnOffSchedule(schedule);
        else if (!request.isStatus() && !schedule.isStatus()) turnOnSchedule(schedule);
    }

    public List<ScheduleResponse> getAll(String id, Pageable pageable) {
        var device = deviceService.getById(id);

        List<DeviceSchedule> schedules = deviceScheduleRepository.findAllByDevice(device, pageable);
        return schedules.stream().map(schedule -> {
            var response = scheduleMapper.toScheduleResponse(schedule);
            if (schedule.isStatus()) {
                response.setRunAfter(Duration.between(LocalDateTime.now(), schedule.getRunAt()).getSeconds());
            } else {
                response.setRunAfter(-1);
            }
            return response;
        }).toList();
    }

    public ScheduleResponse get(String id, String scheduleId) {
        var device = deviceService.getById(id);
        var schedule = deviceScheduleRepository.findByIdAndDevice(scheduleId, device)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_EXISTED));

        var response = scheduleMapper.toScheduleResponse(schedule);
        if (schedule.isStatus()) {
            response.setRunAfter(Duration.between(LocalDateTime.now(), schedule.getRunAt()).getSeconds());
        } else {
            response.setRunAfter(-1);
        }
        return response;
    }

    public long getQuantity(String id) {
        return deviceScheduleRepository.countByDevice(deviceService.getById(id));
    }

}
