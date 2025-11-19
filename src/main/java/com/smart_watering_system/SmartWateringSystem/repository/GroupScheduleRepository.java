package com.smart_watering_system.SmartWateringSystem.repository;

import com.smart_watering_system.SmartWateringSystem.entity.Device;
import com.smart_watering_system.SmartWateringSystem.entity.DeviceSchedule;
import com.smart_watering_system.SmartWateringSystem.entity.Group;
import com.smart_watering_system.SmartWateringSystem.entity.GroupSchedule;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupScheduleRepository extends JpaRepository<GroupSchedule, String> {
    Optional<GroupSchedule> findByIdAndGroup(String id, Group group);
    List<GroupSchedule> findAllByGroup(Group group, Pageable pageable);
    long countByGroup(Group group);
    Optional<GroupSchedule> findFirstByGroupAndStatusIsTrueAndRunAtAfterOrderByRunAtAsc(Group group, LocalDateTime dateTime);
}
