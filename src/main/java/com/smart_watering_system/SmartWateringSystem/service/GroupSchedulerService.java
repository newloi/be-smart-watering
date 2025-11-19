package com.smart_watering_system.SmartWateringSystem.service;

import com.smart_watering_system.SmartWateringSystem.dto.request.ScheduleRequest;
import com.smart_watering_system.SmartWateringSystem.dto.request.TriggerRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ScheduleResponse;
import com.smart_watering_system.SmartWateringSystem.entity.GroupSchedule;
import com.smart_watering_system.SmartWateringSystem.enums.Day;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.enums.Repeat;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import com.smart_watering_system.SmartWateringSystem.mapper.ScheduleMapper;
import com.smart_watering_system.SmartWateringSystem.repository.GroupScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupSchedulerService {

    GroupScheduleRepository groupScheduleRepository;
    GroupService groupService;
    ScheduleMapper scheduleMapper;
    TaskScheduler taskScheduler;
    GroupWateringService groupWateringService;
    Map<String, ScheduledFuture<?>> schedules;

    public ScheduleResponse create(String id, ScheduleRequest request) {
        var group = groupService.getById(id);

        var schedule = scheduleMapper.toGroupSchedule(request);
        schedule.setGroup(group);
        runSchedule(schedule);

        var response = scheduleMapper.toScheduleResponse(schedule);
        if (schedule.isStatus()) {
            response.setRunAfter(Duration.between(LocalDateTime.now(), schedule.getRunAt()).getSeconds());
        } else {
            response.setRunAfter(-1);
        }

        return response;
    }

    public void runSchedule(GroupSchedule schedule) {
        if(!schedule.isStatus()) return;

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
                groupScheduleRepository.save(schedule);
            }
            case DAYS -> {
                String days = schedule.getDaysOfWeek().stream().map(Day::name).collect(Collectors.joining(","));
                cronExpression = String.format("0 %d %d * * %s", startTime.getMinute(), startTime.getHour(), days);

                int currentDayOfWeek = dateNow.getDayOfWeek().getValue();
                int dayStep = schedule.getDaysOfWeek().stream().mapToInt(day ->
                        (day.ordinal() + 1 - currentDayOfWeek + 7) % 7
                ).min().orElseThrow();
                schedule.setRunAt(LocalDateTime.of(dateNow, startTime).plusDays(dayStep));
                groupScheduleRepository.save(schedule);
            }
            case ONE_TIME -> {
                cronExpression = String.format("0 %d %d * * *", startTime.getMinute(), startTime.getHour());

                if (startTime.isBefore(timeNow)) schedule.setRunAt(LocalDateTime.of(dateNow.plusDays(1), startTime));
                else schedule.setRunAt(LocalDateTime.of(dateNow, startTime));
                groupScheduleRepository.save(schedule);

                var scheduler = taskScheduler.schedule(() ->
                        {
                            groupWateringService.runByScheduler(schedule.getGroup().getId(), schedule.getDuration());
                            turnOffSchedule(schedule);
                        },
                        new CronTrigger(cronExpression));

                schedules.put(schedule.getId(), scheduler);

                return;
            }
        }

        var scheduler = taskScheduler.schedule(() -> {
                    Repeat type = schedule.getRepeatType();
                    groupWateringService.runByScheduler(schedule.getGroup().getId(), schedule.getDuration());
                    if (type == Repeat.EVERYDAY) {
                        schedule.setRunAt(schedule.getRunAt().plusDays(1));
                        groupScheduleRepository.save(schedule);
                    } else if (type == Repeat.DAYS) {
                        int currentDayOfWeek = LocalDate.now().getDayOfWeek().getValue();
                        int dayStep = schedule.getDaysOfWeek().stream().mapToInt(day ->
                                (day.ordinal() + 1 - currentDayOfWeek + 6) % 7 + 1
                        ).min().orElseThrow();
                        schedule.setRunAt(schedule.getRunAt().plusDays(dayStep));
                        groupScheduleRepository.save(schedule);
                    }
                },
                new CronTrigger(cronExpression));

        schedules.put(schedule.getId(), scheduler);
    }

    public void turnOffSchedule(GroupSchedule schedule) {
        if(!schedule.isStatus()) return;

        schedules.get(schedule.getId()).cancel(true);
        schedules.remove(schedule.getId());
        schedule.setStatus(false);
        groupScheduleRepository.save(schedule);
    }

    public void turnOnSchedule(GroupSchedule schedule) {
        if(schedule.isStatus()) return;

        schedule.setStatus(true);
        schedule = groupScheduleRepository.save(schedule);
        runSchedule(schedule);
    }

    public void delete(String id, String scheduleId) {
        var group = groupService.getById(id);
        var schedule = groupScheduleRepository.findByIdAndGroup(scheduleId, group)
                        .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_EXISTED));

        if(schedules.containsKey(schedule.getId())) {
            schedules.get(schedule.getId()).cancel(true);
            schedules.remove(schedule.getId());
        }
        groupScheduleRepository.delete(schedule);
    }

    public ScheduleResponse update(String id, String scheduleId, ScheduleRequest request) {
        var group = groupService.getById(id);
        var schedule = groupScheduleRepository.findByIdAndGroup(scheduleId, group)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_EXISTED));

        scheduleMapper.updateSchedule(schedule, request);
        schedule = groupScheduleRepository.save(schedule);

        if(schedule.isStatus()) {
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
        var group = groupService.getById(id);
        var schedule = groupScheduleRepository.findByIdAndGroup(scheduleId, group)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_EXISTED));

        if (request.isStatus() && schedule.isStatus()) turnOffSchedule(schedule);
        else if(!request.isStatus() && !schedule.isStatus()) turnOnSchedule(schedule);
    }

    public List<ScheduleResponse> getAll(String id, Pageable pageable) {
        var group = groupService.getById(id);

        List<GroupSchedule> schedules = groupScheduleRepository.findAllByGroup(group, pageable);
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
        var group = groupService.getById(id);
        var schedule = groupScheduleRepository.findByIdAndGroup(scheduleId, group)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_EXISTED));

        var response = scheduleMapper.toScheduleResponse(schedule);
        if (schedule.isStatus()) {
            response.setRunAfter(Duration.between(LocalDateTime.now(), schedule.getRunAt()).getSeconds());
        } else {
            response.setRunAfter(-1);
        }
        return response;
    }

    public long getQuantity(String groupId) {
        return groupScheduleRepository.countByGroup(groupService.getById(groupId));
    }

}
