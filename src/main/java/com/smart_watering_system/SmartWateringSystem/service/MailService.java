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

    public void sendOtpEmail(String desEmail, String otpCode) throws IOException, MessagingException {
        ClassPathResource resource = new ClassPathResource("templates/otp-template.html");
        String htmlContent = Files.readString(resource.getFile().toPath());

        htmlContent = htmlContent.replace("${otp}", otpCode);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        helper.setFrom(srcEmail);
        helper.setTo(desEmail);
        helper.setSubject("Smart Watering - Mã xác thực OTP của bạn");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

}
