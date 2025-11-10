package com.smart_watering_system.SmartWateringSystem.repository;

import com.smart_watering_system.SmartWateringSystem.entity.Group;
import com.smart_watering_system.SmartWateringSystem.entity.GroupWateringHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupWateringHistoryRepository extends JpaRepository<GroupWateringHistory, String> {
    List<GroupWateringHistory> findAllByGroup(Group group, Pageable pageable);
    long countByGroup(Group group);
}
