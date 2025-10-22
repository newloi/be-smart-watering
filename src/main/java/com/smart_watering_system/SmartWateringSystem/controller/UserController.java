package com.smart_watering_system.SmartWateringSystem.controller;

import com.smart_watering_system.SmartWateringSystem.dto.request.UserRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.UserResponse;
import com.smart_watering_system.SmartWateringSystem.mapper.UserMapper;
import com.smart_watering_system.SmartWateringSystem.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/users")
public class UserController {

    UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserRequest request) throws MessagingException, IOException {
        return ApiResponse.<UserResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .data(userService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<UserResponse> getUser(@RequestHeader("Authorization") String headerAuthorization) {
        return ApiResponse.<UserResponse>builder()
                .data(userMapper.toUserResponse(userService.getUser(headerAuthorization)))
                .build();
    }

}
