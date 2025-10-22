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

    public ScheduleResponse create(String id, ScheduleRequest request, String authHeader) {
        var group = groupService.getByUser(id, authHeader);

        var schedule = scheduleMapper.toGroupSchedule(request);
        schedule.setGroup(group);
        schedule = groupScheduleRepository.save(schedule);

        runSchedule(schedule);

        return scheduleMapper.toScheduleResponse(schedule);
    }

    public void runSchedule(GroupSchedule schedule) {
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
                            groupWateringService.runByScheduler(schedule.getGroup().getId(), schedule.getDuration());
                            turnOffSchedule(schedule);
                        },
                        new CronTrigger(cronExpression));

                schedules.put(schedule.getId(), scheduler);

                return;
            }
        }

        var scheduler = taskScheduler.schedule(() ->
                groupWateringService.runByScheduler(schedule.getGroup().getId(), schedule.getDuration()),
                new CronTrigger(cronExpression));

        schedules.put(schedule.getId(), scheduler);
    }

    public void turnOffSchedule(GroupSchedule schedule) {
        if(!schedule.isStatus()) return;

        schedules.get(schedule.getId()).cancel(true);
        schedule.setStatus(false);
        groupScheduleRepository.save(schedule);
    }

    public void turnOnSchedule(GroupSchedule schedule) {
        if(schedule.isStatus()) return;

        schedules.get(schedule.getId()).cancel(true);

        schedule.setStatus(true);
        schedule = groupScheduleRepository.save(schedule);
        runSchedule(schedule);
    }

    public void delete(String authHeader, String id, String scheduleId) {
        var group = groupService.getByUser(id, authHeader);
        var schedule = groupScheduleRepository.findByIdAndGroup(scheduleId, group)
                        .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_EXISTED));

        schedules.get(schedule.getId()).cancel(true);
        schedules.remove(schedule.getId());
        groupScheduleRepository.delete(schedule);
    }

    public ScheduleResponse update(String authHeader, String id, String scheduleId, ScheduleRequest request) {
        var group = groupService.getByUser(id, authHeader);
        var schedule = groupScheduleRepository.findByIdAndGroup(scheduleId, group)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_EXISTED));

        scheduleMapper.updateSchedule(schedule, request);
        schedule = groupScheduleRepository.save(schedule);

        if(schedule.isStatus()) {
            schedules.get(schedule.getId()).cancel(true);
            runSchedule(schedule);
        }

        return scheduleMapper.toScheduleResponse(schedule);
    }

    public void trigger(String authHeader, String id, String scheduleId, TriggerRequest request) {
        var group = groupService.getByUser(id, authHeader);
        var schedule = groupScheduleRepository.findByIdAndGroup(scheduleId, group)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_EXISTED));

        if (request.isStatus() && schedule.isStatus()) turnOffSchedule(schedule);
        else if(!request.isStatus() && !schedule.isStatus()) turnOnSchedule(schedule);
    }

    public List<ScheduleResponse> getAll(String id, String authHeader, Pageable pageable) {
        var group = groupService.getByUser(id, authHeader);

        List<GroupSchedule> schedules = groupScheduleRepository.findAllByGroup(group, pageable);
        return schedules.stream().map(scheduleMapper::toScheduleResponse).toList();
    }

}
