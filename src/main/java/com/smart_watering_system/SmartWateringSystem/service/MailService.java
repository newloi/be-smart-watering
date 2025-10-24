package com.smart_watering_system.SmartWateringSystem.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MailService {

    @NonFinal
    @Value("${spring.mail.username}")
    String srcEmail;

    JavaMailSender mailSender;
    SpringTemplateEngine templateEngine;

    public void sendOtpEmail(String desEmail, String otpCode) throws MessagingException {
        Context context = new Context();
        context.setVariable("otpCode", otpCode);

        String htmlContent = templateEngine.process("otp-template", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setFrom(srcEmail);
        helper.setTo(desEmail);
        helper.setSubject("Smart Watering - Mã xác thực OTP");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

}
