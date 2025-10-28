package com.smart_watering_system.SmartWateringSystem.controller;

import com.smart_watering_system.SmartWateringSystem.annotation.RateLimiter;
import com.smart_watering_system.SmartWateringSystem.dto.request.SendEmailRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.service.MailService;
import com.smart_watering_system.SmartWateringSystem.service.TokenService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/mail")
public class MailController {

    MailService mailService;
    TokenService tokenService;

    @PostMapping("/send")
    @RateLimiter(capacity = 1, refillTokens = 1, refillPeriodSeconds = 60)
    ApiResponse<Void> sendOtpMail(@RequestBody @Valid SendEmailRequest request) throws IOException {
        String desEmail = request.getEmail();
        mailService.sendOtpEmailAsync(desEmail, tokenService.generateOtp(desEmail));

        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .build();
    }

}
