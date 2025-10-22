package com.smart_watering_system.SmartWateringSystem.repository;

import com.smart_watering_system.SmartWateringSystem.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, String> {
    void deleteByEmail(String email);
    Optional<Otp> findByEmail(String email);
}
