package com.smart_watering_system.SmartWateringSystem.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RateLimitService {

    Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(
            String username, String methodKey, int capacity, int refillTokens, long refillPeriodSeconds) {
        String key = username + ":" + methodKey;

        return cache.computeIfAbsent(key, k ->
                Bucket.builder()
                        .addLimit(limit -> limit
                                .capacity(capacity)
                                .refillGreedy(refillTokens, Duration.ofSeconds(refillPeriodSeconds))
                        )
                        .build()
        );
    }

}
