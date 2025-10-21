package com.smart_watering_system.SmartWateringSystem.repository;

import com.smart_watering_system.SmartWateringSystem.entity.DataSensorHistory;
import com.smart_watering_system.SmartWateringSystem.entity.Device;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataSensorHistoryRepository extends JpaRepository<DataSensorHistory, String> {
    Optional<DataSensorHistory> findTopByDeviceOrderByTimestampDesc(Device device);
    List<DataSensorHistory> findAllByDeviceOrderByTimestampDesc(Device device, Pageable pageable);
}
