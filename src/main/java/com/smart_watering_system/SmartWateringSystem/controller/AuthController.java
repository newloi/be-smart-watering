package com.smart_watering_system.SmartWateringSystem.controller;

import com.nimbusds.jose.JOSEException;
import com.smart_watering_system.SmartWateringSystem.dto.request.ChangePasswordRequest;
import com.smart_watering_system.SmartWateringSystem.dto.request.IntrospectRequest;
import com.smart_watering_system.SmartWateringSystem.dto.request.LoginRequest;
import com.smart_watering_system.SmartWateringSystem.dto.request.VerifyRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.IntrospectResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.LoginResponse;
import com.smart_watering_system.SmartWateringSystem.service.AuthService;
import com.smart_watering_system.SmartWateringSystem.service.TokenService;
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
    TokenService tokenService;

    @PostMapping("/log-in")
    ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(authService.login(request))
                .build();
    }

    @PostMapping("/log-out")
    ApiResponse<Void> logout(@RequestBody @Valid IntrospectRequest request) throws ParseException, JOSEException {
        var token = request.getToken();
        authService.logout(token.startsWith("Bearer ") ? token.substring(7) : token);

        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody @Valid IntrospectRequest request) throws ParseException, JOSEException {
        var token = request.getToken();

        return ApiResponse.<IntrospectResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(tokenService.introspect(token.startsWith("Bearer ") ? token.substring(7) : token))
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<LoginResponse> refreshToken(@RequestBody @Valid IntrospectRequest request) throws ParseException, JOSEException {
        var token = request.getToken();

        return ApiResponse.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(tokenService.refreshToken(token.startsWith("Bearer ") ? token.substring(7) : token))
                .build();
    }

    @PostMapping("/verify")
    ApiResponse<Void> verifyOtp(@RequestBody @Valid VerifyRequest request) {
        tokenService.verifyOtp(request);

        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .build();
    }

    @PostMapping("/change-password")
    ApiResponse<Void> changePassword(@RequestHeader("Authorization") String authHeader,
                                     @RequestBody @Valid ChangePasswordRequest request) throws ParseException {
        authService.changePassword(authHeader, request);

        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .build();
    }

}
