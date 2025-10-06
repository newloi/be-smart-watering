package com.smart_watering_system.SmartWateringSystem.controller;

import com.nimbusds.jose.JOSEException;
import com.smart_watering_system.SmartWateringSystem.dto.request.LoginRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.IntrospectResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.LoginResponse;
import com.smart_watering_system.SmartWateringSystem.service.AuthService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/auth")
public class AuthController {

    AuthService authService;

    @PostMapping("/log-in")
    ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(authService.login(request))
                .build();
    }

    @PostMapping("/log-out")
    ApiResponse<Void> logout(@RequestHeader("Authorization") String token) throws ParseException, JOSEException {
        authService.logout(token.startsWith("Bearer ") ? token.substring(7) : token);

        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestHeader("Authorization") String token) throws ParseException, JOSEException {
        return ApiResponse.<IntrospectResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(authService.introspect(token.startsWith("Bearer ") ? token.substring(7) : token))
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<LoginResponse> refreshToken(@RequestHeader("Authorization") String token) throws ParseException, JOSEException {
        return ApiResponse.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(authService.refreshToken(token.startsWith("Bearer ") ? token.substring(7) : token))
                .build();
    }

}
