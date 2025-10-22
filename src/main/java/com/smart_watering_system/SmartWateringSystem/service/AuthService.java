package com.smart_watering_system.SmartWateringSystem.service;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.SignedJWT;
import com.smart_watering_system.SmartWateringSystem.dto.request.ChangePasswordRequest;
import com.smart_watering_system.SmartWateringSystem.dto.request.LoginRequest;
import com.smart_watering_system.SmartWateringSystem.dto.request.VerifyRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.LoginResponse;
import com.smart_watering_system.SmartWateringSystem.entity.User;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import com.smart_watering_system.SmartWateringSystem.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    UserService userService;
    UserRepository userRepository;
    TokenService tokenService;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.accessDuration}")
    long accessDuration;

    @NonFinal
    @Value("${jwt.refreshDuration}")
    long refreshDuration;

    public LoginResponse login(LoginRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.isVerified()) throw new AppException(ErrorCode.ACC_NOT_VERIFIED);

        boolean isAuthenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!isAuthenticated) throw new AppException(ErrorCode.WRONG_PASSWORD);

        return LoginResponse.builder()
                .accessToken(tokenService.generateToken(user, accessDuration))
                .refreshToken(tokenService.generateToken(user, refreshDuration))
                .build();
    }

    public void logout(String accessToken, String refreshToken) throws JOSEException, ParseException {
        try {
            SignedJWT signedAccessToken = tokenService.verifyToken(accessToken);
            SignedJWT signedRefreshToken = tokenService.verifyToken(refreshToken);

            var usernameInAccessToken = signedAccessToken.getJWTClaimsSet().getSubject();
            var usernameInRefreshToken = signedRefreshToken.getJWTClaimsSet().getSubject();

            if(!Objects.equals(usernameInAccessToken, usernameInRefreshToken))
                throw new AppException(ErrorCode.INVALID_TOKEN);

            tokenService.deleteToken(signedAccessToken);
            tokenService.deleteToken(signedRefreshToken);
        } catch (AppException e) {
            if(e.getErrorCode() != ErrorCode.EXPIRED_TOKEN) throw e;
        }
    }

    public void changePassword(String authHeader, ChangePasswordRequest request) throws ParseException {
        User user = userService.getUser(authHeader);

        if(!Objects.equals(request.getNewPassword(), request.getConfirmNewPassword()))
            throw new AppException(ErrorCode.PASS_NOT_MATCH);

        String token = authHeader.split(" ")[1];
        SignedJWT signedJWT = SignedJWT.parse(token);
        String email = signedJWT.getJWTClaimsSet().getClaim("email").toString();

        tokenService.verifyOtp(VerifyRequest.builder().email(email).code(request.getCode()).build());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
    }

}
