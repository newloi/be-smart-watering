package com.smart_watering_system.SmartWateringSystem.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RateLimitService {

    Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(1000)
            .build();

    public Bucket resolveBucket(
            String username, String methodKey, int capacity, int refillTokens, long refillPeriodSeconds) {
        String key = username + ":" + methodKey;

        return cache.get(key, k ->
                Bucket.builder()
                        .addLimit(limit -> limit
                                .capacity(capacity)
                                .refillGreedy(refillTokens, Duration.ofSeconds(refillPeriodSeconds))
                        )
                        .build()
        );
    }

}
