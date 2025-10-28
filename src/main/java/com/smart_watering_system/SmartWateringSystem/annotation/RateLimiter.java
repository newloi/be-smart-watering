package com.smart_watering_system.SmartWateringSystem.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {
    int capacity() default 20;
    int refillTokens() default 20;
    long refillPeriodSeconds() default 10L;
}
