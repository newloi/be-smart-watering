package com.smart_watering_system.SmartWateringSystem.job;

import com.smart_watering_system.SmartWateringSystem.entity.InvalidatedToken;
import com.smart_watering_system.SmartWateringSystem.repository.InvalidatedTokenRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Job {

    InvalidatedTokenRepository invalidatedTokenRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanInvalidateTokenTable() {
        List<InvalidatedToken> invalidatedTokens = invalidatedTokenRepository.findByExpiryTimeBefore(new Date());

        invalidatedTokenRepository.deleteAll(invalidatedTokens);
    }

}
