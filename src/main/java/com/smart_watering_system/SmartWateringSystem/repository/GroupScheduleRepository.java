package com.smart_watering_system.SmartWateringSystem.repository;

import com.smart_watering_system.SmartWateringSystem.entity.Group;
import com.smart_watering_system.SmartWateringSystem.entity.GroupSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupScheduleRepository extends JpaRepository<GroupSchedule, String> {
    Optional<GroupSchedule> findByIdAndGroup(String id, Group group);
}
