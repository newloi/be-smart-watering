package com.smart_watering_system.SmartWateringSystem.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smart_watering_system.SmartWateringSystem.dto.request.ScheduleRequest;
import com.smart_watering_system.SmartWateringSystem.entity.DeviceSchedule;
import com.smart_watering_system.SmartWateringSystem.enums.Day;
import com.smart_watering_system.SmartWateringSystem.enums.Repeat;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceScheduleRepository;
import com.smart_watering_system.SmartWateringSystem.service.DeviceWateringService;
import com.smart_watering_system.SmartWateringSystem.service.SchedulerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppInitConfig {

    @Bean
    ApplicationRunner applicationRunner(DeviceScheduleRepository deviceScheduleRepository,
                                        SchedulerService schedulerService) {
        return args -> {
            List<DeviceSchedule> deviceSchedules = deviceScheduleRepository.findAll();
            deviceSchedules.forEach(schedulerService::runSchedule);
        };
    }

    @Bean
    Map<String, ScheduledFuture<?>> allSchedules() {
        return new ConcurrentHashMap<>();
    }

}
