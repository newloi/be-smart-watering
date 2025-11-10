package com.smart_watering_system.SmartWateringSystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smart_watering_system.SmartWateringSystem.dto.request.WateringRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.WateringResponse;
import com.smart_watering_system.SmartWateringSystem.entity.*;
import com.smart_watering_system.SmartWateringSystem.enums.Action;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import com.smart_watering_system.SmartWateringSystem.mapper.WateringMapper;
import com.smart_watering_system.SmartWateringSystem.repository.GroupRepository;
import com.smart_watering_system.SmartWateringSystem.repository.GroupWateringHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupWateringService {

    GroupRepository groupRepository;
    DeviceWateringService deviceWateringService;
    Executor taskExecutor;
    WateringMapper wateringMapper;
    GroupWateringHistoryRepository groupWateringHistoryRepository;
    UserService userService;
    GroupService groupService;
    RealtimeService realtimeService;
    Map<String, ScheduledFuture<?>> schedules;
    TaskScheduler taskScheduler;

    public WateringResponse doAction(String id, WateringRequest request) {
        var user = userService.getUser();
        Group group = groupRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_EXISTED));

        group.getDevices().forEach(device -> {
            taskExecutor.execute(() -> {
                try {
                    deviceWateringService.doAction(device.getId(), request, true, null);
                } catch (MqttException | JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        Action action = request.getAction();
        GroupWateringHistory recentWatering = group.getHistories().isEmpty() ? null : group.getHistories().getFirst();
        boolean isRunning = !Objects.isNull(recentWatering)
                && LocalDateTime.now().isBefore(recentWatering.getStartTime().plusSeconds(recentWatering.getDuration()));

        if (action == Action.START) {
            if (!isRunning) {
                realtimeService.sendGroupWateringStatus(group, true);
                LocalDateTime stopAt = LocalDateTime.now().plusSeconds(request.getDuration());
                String cronExpression = String.format("%d %d %d * * *", stopAt.getSecond(), stopAt.getMinute(), stopAt.getHour());
                var scheduler = taskScheduler.schedule(() -> stopWateringGroup(group),
                        new CronTrigger(cronExpression));
                schedules.put(group.getId(), scheduler);

                GroupWateringHistory history = wateringMapper.toGroupWateringHistory(request);
                history.setGroup(group);
                history = groupWateringHistoryRepository.save(history);

                var response = wateringMapper.toWateringResponse(history);
                response.setAction(action);

                return response;
            }

        } else if (action == Action.STOP) {
            if (isRunning) {
                stopWateringGroup(group);

                recentWatering.setDuration(
                        ChronoUnit.SECONDS.between(recentWatering.getStartTime(), LocalDateTime.now())
                );
                var history = groupWateringHistoryRepository.save(recentWatering);

                var response = wateringMapper.toWateringResponse(history);
                response.setAction(action);

                return response;
            }
        } else throw new AppException(ErrorCode.INVALID_ACTION);

        return WateringResponse.builder().build();

    }

    public List<WateringResponse> getAllHistories(String id, Pageable pageable) {
        Group group = groupRepository.findByIdAndUser(id, userService.getUser())
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_EXISTED));

        List<GroupWateringHistory> histories = groupWateringHistoryRepository
                .findAllByGroup(group, pageable);
        return histories.stream().map(wateringMapper::toWateringResponse).toList();
    }

    @Transactional
    public void runByScheduler(String id, long duration) {
        Group group = groupRepository.findByIdWithHistories(id);

        group.getDevices().forEach(device -> {
            taskExecutor.execute(() -> {
                deviceWateringService.runByScheduler(device.getId(), duration, true);
            });
        });

        GroupWateringHistory recentWatering = group.getHistories().isEmpty() ? null : group.getHistories().getFirst();
        boolean isRunning = !Objects.isNull(recentWatering) && LocalDateTime.now().isBefore(recentWatering.getStartTime()
                .plusSeconds(recentWatering.getDuration()));

        if (!isRunning) {
            WateringRequest request = new WateringRequest(Action.START, duration);

            GroupWateringHistory history = wateringMapper.toGroupWateringHistory(request);
            history.setGroup(group);
            groupWateringHistoryRepository.save(history);
        }
    }

    public long getQuantity(String groupId) {
        return groupWateringHistoryRepository.countByGroup(groupService.getById(groupId));
    }

    private void stopWateringGroup(Group group) {
        realtimeService.sendGroupWateringStatus(group, false);

        String groupId = group.getId();
        if(schedules.containsKey(groupId)) {
            schedules.get(groupId).cancel(true);
            schedules.remove(groupId);
        }
    }

}