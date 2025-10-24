package com.smart_watering_system.SmartWateringSystem.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MailService {

    @NonFinal
    @Value("${spring.mail.username}")
    String srcEmail;

    @NonFinal
    @Value("${sendgrid.api-key}")
    String sendGridApiKey;

    SpringTemplateEngine templateEngine;

    @Async
    public void sendOtpEmailAsync(String desEmail, String otpCode) throws IOException {
        Context context = new Context();
        context.setVariable("otpCode", otpCode);

        String htmlContent = templateEngine.process("otp-template", context);

        Email from = new Email(srcEmail);
        String subject = "Smart Watering - Mã xác thực OTP";
        Email to = new Email(desEmail);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sendGrid = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sendGrid.api(request);
        log.info("SendGrid response Status: {}", response.getStatusCode());
        log.info("SendGrid response Body: {}", response.getBody());
    }

}
