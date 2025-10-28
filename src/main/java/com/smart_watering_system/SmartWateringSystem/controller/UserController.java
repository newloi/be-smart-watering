package com.smart_watering_system.SmartWateringSystem.controller;

import com.smart_watering_system.SmartWateringSystem.annotation.RateLimiter;
import com.smart_watering_system.SmartWateringSystem.dto.request.UserRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.UserResponse;
import com.smart_watering_system.SmartWateringSystem.mapper.UserMapper;
import com.smart_watering_system.SmartWateringSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/users")
public class UserController {

    UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    @RateLimiter(capacity = 10, refillTokens = 10, refillPeriodSeconds = 60)
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserRequest request) throws IOException {
        return ApiResponse.<UserResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .data(userService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<UserResponse> getUser() {
        return ApiResponse.<UserResponse>builder()
                .data(userMapper.toUserResponse(userService.getUser()))
                .build();
    }

}
