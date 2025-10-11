package com.smart_watering_system.SmartWateringSystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smart_watering_system.SmartWateringSystem.dto.request.WateringRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.WateringResponse;
import com.smart_watering_system.SmartWateringSystem.entity.*;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import com.smart_watering_system.SmartWateringSystem.mapper.WateringMapper;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceWateringHistoryRepository;
import com.smart_watering_system.SmartWateringSystem.repository.GroupRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupWateringService {

    GroupRepository groupRepository;
    DeviceWateringService deviceWateringService;
    Executor executor;

    public void doAction(String id, WateringRequest request, User user) {
        Group group = groupRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_EXISTED));

        group.getDevices().forEach(device -> {
            executor.execute(() -> {
                try {
                    deviceWateringService.doAction(device.getId(), request, user, true);
                } catch (MqttException | JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

}
