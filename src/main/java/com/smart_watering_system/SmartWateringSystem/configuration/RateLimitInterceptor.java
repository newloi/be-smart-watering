package com.smart_watering_system.SmartWateringSystem.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart_watering_system.SmartWateringSystem.annotation.RateLimiter;
import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.service.RateLimitService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.text.ParseException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RateLimitInterceptor implements HandlerInterceptor {

    RateLimitService rateLimitService;
    ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String username = extractUsernameFromSecurityContext().orElseGet(() -> extractIpAsFallback(request));
        String methodKey = request.getMethod() + " " + request.getRequestURI();

        int capacity = 20, refillTokens = 20;
        long refillPeriodSeconds = 60L;
        if(handler instanceof HandlerMethod handlerMethod) {
            RateLimiter rateLimiter = handlerMethod.getMethodAnnotation(RateLimiter.class);
            if(rateLimiter == null) rateLimiter = handlerMethod.getBeanType().getAnnotation(RateLimiter.class);
            if(rateLimiter != null) {
                capacity = rateLimiter.capacity();
                refillTokens = rateLimiter.refillTokens();
                refillPeriodSeconds = rateLimiter.refillPeriodSeconds();
                methodKey = handlerMethod.getBeanType().getSimpleName() + "#" + handlerMethod.getMethod().getName();
            }
        }

        Bucket bucket = rateLimitService.resolveBucket(username, methodKey, capacity, refillTokens, refillPeriodSeconds);
        boolean consumed = bucket.tryConsume(1);
        if(!consumed) {
            ErrorCode errorCode = ErrorCode.TOO_MANY_REQUESTS;
            response.setStatus(errorCode.getStatusCode());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ApiResponse<?> apiResponse = ApiResponse.builder()
                    .statusCode(errorCode.getStatusCode())
                    .message(errorCode.getMessage())
                    .build();

            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            response.flushBuffer();

            return false;
        }

        return true;
    }

    private Optional<String> extractUsernameFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null
                && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser")) {
           return Optional.of(authentication.getName());
        }

        return Optional.empty();
    }

    private String extractIpAsFallback(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }

        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "anonymous";
    }

}
