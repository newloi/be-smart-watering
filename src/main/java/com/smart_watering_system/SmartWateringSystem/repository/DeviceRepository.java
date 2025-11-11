package com.smart_watering_system.SmartWateringSystem.repository;

import com.smart_watering_system.SmartWateringSystem.entity.Device;
import com.smart_watering_system.SmartWateringSystem.entity.Group;
import com.smart_watering_system.SmartWateringSystem.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findAllByUser(User user, Pageable pageable);
    Optional<Device> findByIdAndUser(String id, User user);
    List<Device> findByGroupIsNullAndUser(User user, Pageable pageable);
    Optional<Device> findByDeviceId(String deviceId);

    @Query("SELECT d FROM Device d LEFT JOIN FETCH d.histories WHERE d.id = :id")
    Device findByIdWithHistories(@Param("id") String id);

    @Query("SELECT d FROM Device d LEFT JOIN FETCH d.histories WHERE d.id = :id AND d.user = :user")
    Optional<Device> findByIdAndUserWithHistories(@Param("id") String id, @Param("user") User user);

    List<Device> findByUserAndNameContainingIgnoreCase(User user, String keyword, Pageable pageable);

    long countByUser(User user);
    long countByUserAndGroupIsNull(User user);
    long countByUserAndNameContainingIgnoreCase(User user, String keyword);
    long countByGroup(Group group);
}
