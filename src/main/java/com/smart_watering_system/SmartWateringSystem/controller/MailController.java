package com.smart_watering_system.SmartWateringSystem.controller;

import com.smart_watering_system.SmartWateringSystem.dto.request.SendEmailRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.service.AuthService;
import com.smart_watering_system.SmartWateringSystem.service.MailService;
import jakarta.mail.MessagingException;
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
    AuthService authService;

    @PostMapping("/send")
    ApiResponse<Void> sendOtpMail(@RequestBody SendEmailRequest request) throws MessagingException, IOException {
        String desEmail = request.getEmail();
        mailService.sendOtpEmail(desEmail, authService.generateOtp(desEmail));

        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .build();
    }

}
