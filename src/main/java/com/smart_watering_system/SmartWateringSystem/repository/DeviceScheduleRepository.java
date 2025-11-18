package com.smart_watering_system.SmartWateringSystem.repository;

import com.smart_watering_system.SmartWateringSystem.entity.Device;
import com.smart_watering_system.SmartWateringSystem.entity.DeviceSchedule;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceScheduleRepository extends JpaRepository<DeviceSchedule, String> {
    Optional<DeviceSchedule> findByIdAndDevice(String id, Device device);
    List<DeviceSchedule> findAllByDevice(Device device, Pageable pageable);
    long countByDevice(Device device);
    Optional<DeviceSchedule> findFirstByDeviceAndStatusIsTrueAndRunAtAfterOrderByRunAtAsc(Device device, LocalDateTime dateTime);
}
