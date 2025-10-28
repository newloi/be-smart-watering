package com.smart_watering_system.SmartWateringSystem.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smart_watering_system.SmartWateringSystem.entity.DeviceSchedule;
import com.smart_watering_system.SmartWateringSystem.entity.GroupSchedule;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceScheduleRepository;
import com.smart_watering_system.SmartWateringSystem.repository.GroupScheduleRepository;
import com.smart_watering_system.SmartWateringSystem.service.DeviceSchedulerService;
import com.smart_watering_system.SmartWateringSystem.service.GroupSchedulerService;
import com.smart_watering_system.SmartWateringSystem.service.MqttSevice;
import com.smart_watering_system.SmartWateringSystem.service.RealtimeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppInitConfig {

    @Bean
    ApplicationRunner applicationRunner(DeviceScheduleRepository deviceScheduleRepository,
                                        DeviceSchedulerService deviceSchedulerService,
                                        GroupScheduleRepository groupScheduleRepository,
                                        GroupSchedulerService groupSchedulerService,
                                        MqttSevice mqttSevice,
                                        RealtimeService realtimeService) {
        return args -> {
            mqttSevice.subcribeAsync("status/#",
                    (topic, message) -> realtimeService.sendDeviceStatusAsync(topic, message.toString()));

            mqttSevice.subcribeAsync("sensor/#",
                    (topic, message) -> realtimeService.sendDataAsync(topic, message.toString()));

            List<DeviceSchedule> deviceSchedules = deviceScheduleRepository.findAll();
            deviceSchedules.forEach(deviceSchedulerService::runSchedule);

            List<GroupSchedule> groupSchedules = groupScheduleRepository.findAll();
            groupSchedules.forEach(groupSchedulerService::runSchedule);
        };
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

}
