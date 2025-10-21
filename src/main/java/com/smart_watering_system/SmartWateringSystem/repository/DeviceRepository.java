package com.smart_watering_system.SmartWateringSystem.repository;

import com.smart_watering_system.SmartWateringSystem.entity.Device;
import com.smart_watering_system.SmartWateringSystem.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findAllByUser(User user, Pageable pageable);
    Optional<Device> findByIdAndUser(String id, User user);
    void deleteByIdAndUser(String id, User user);
    List<Device> findByGroupIsNullAndUser(User user);
}
