package com.smart_watering_system.SmartWateringSystem.repository;

import com.smart_watering_system.SmartWateringSystem.entity.Group;
import com.smart_watering_system.SmartWateringSystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
    List<Group> findAllByUser(User user);
    Optional<Group> findByIdAndUser(String id, User user);
    void deleteByIdAndUser(String id, User user);

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.histories WHERE g.id = :id")
    Group findByIdWithHistories(@Param("id") String id);
}
