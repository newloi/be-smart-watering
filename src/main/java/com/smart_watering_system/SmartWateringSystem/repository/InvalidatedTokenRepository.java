package com.smart_watering_system.SmartWateringSystem.repository;

import com.smart_watering_system.SmartWateringSystem.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    List<InvalidatedToken> findByExpiryTimeBefore(Date expiryTimeBefore);
}
