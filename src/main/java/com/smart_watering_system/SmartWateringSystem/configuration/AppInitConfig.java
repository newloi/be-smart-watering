package com.smart_watering_system.SmartWateringSystem.configuration;

import com.smart_watering_system.SmartWateringSystem.entity.DeviceSchedule;
import com.smart_watering_system.SmartWateringSystem.repository.DeviceScheduleRepository;
import com.smart_watering_system.SmartWateringSystem.service.SchedulerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
                                        SchedulerService schedulerService) {
        return args -> {
            List<DeviceSchedule> deviceSchedules = deviceScheduleRepository.findAll();
            deviceSchedules.forEach(schedulerService::runSchedule);
        };
    }

}
