package com.smart_watering_system.SmartWateringSystem.service;

import com.nimbusds.jwt.SignedJWT;
import com.smart_watering_system.SmartWateringSystem.dto.request.UserRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.UserResponse;
import com.smart_watering_system.SmartWateringSystem.entity.InvalidatedToken;
import com.smart_watering_system.SmartWateringSystem.entity.User;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import com.smart_watering_system.SmartWateringSystem.mapper.UserMapper;
import com.smart_watering_system.SmartWateringSystem.repository.InvalidatedTokenRepository;
import com.smart_watering_system.SmartWateringSystem.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    MailService mailService;
    AuthService authService;

    public UserResponse create(UserRequest request) throws MessagingException, IOException {
        var user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerified(false);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            String message = e.getRootCause().getMessage();
            if(message.contains("uk_user_email")) throw new AppException(ErrorCode.EMAIL_USED);
            else if(message.contains("uk_user_username")) throw new AppException(ErrorCode.USER_EXISTED);
        }

        String desEmail = request.getEmail();
        mailService.sendOtpEmail(desEmail, authService.generateOtp(desEmail));
        return userMapper.toUserResponse(user);
    }

    public User getUser(String headerAuthorization) {
        String token = headerAuthorization.startsWith("Bearer ") ? headerAuthorization.substring(7) : headerAuthorization;

        String username = null;
        try {
            SignedJWT signedToken = SignedJWT.parse(token);
            username = signedToken.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new JwtException(e.getMessage());
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

}
