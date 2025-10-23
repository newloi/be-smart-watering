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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/auth")
public class AuthController {

    AuthService authService;
    TokenService tokenService;

    @PostMapping("/log-in")
    ApiResponse<LoginResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);

        Cookie cookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/auth");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(cookie);

        return ApiResponse.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(loginResponse)
                .build();
    }

    @PostMapping("/log-out")
    ApiResponse<Void> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                             @RequestBody(required = false) IntrospectRequest request) throws ParseException, JOSEException {

        authService.logout(Objects.isNull(refreshToken) ? request.getRefreshToken() : refreshToken);

        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody @Valid IntrospectRequest request)
            throws ParseException, JOSEException {
        var token = request.getAccessToken();

        return ApiResponse.<IntrospectResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(tokenService.introspect(token))
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<LoginResponse> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                            @RequestBody(required = false) IntrospectRequest request)
            throws ParseException, JOSEException {

        return ApiResponse.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(tokenService.refreshToken(Objects.isNull(refreshToken) ? request.getRefreshToken() : refreshToken))
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
