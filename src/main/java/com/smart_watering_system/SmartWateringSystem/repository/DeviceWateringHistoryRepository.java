package com.smart_watering_system.SmartWateringSystem.repository;

import com.smart_watering_system.SmartWateringSystem.entity.Device;
import com.smart_watering_system.SmartWateringSystem.entity.DeviceWateringHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceWateringHistoryRepository extends JpaRepository<DeviceWateringHistory, String> {
    List<DeviceWateringHistory> findAllByDevice(Device device, Pageable pageable);
    long countByDevice(Device device);
}
