package com.smart_watering_system.SmartWateringSystem.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.smart_watering_system.SmartWateringSystem.dto.request.VerifyRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.IntrospectResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.LoginResponse;
import com.smart_watering_system.SmartWateringSystem.entity.InvalidatedToken;
import com.smart_watering_system.SmartWateringSystem.entity.Otp;
import com.smart_watering_system.SmartWateringSystem.entity.User;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import com.smart_watering_system.SmartWateringSystem.repository.InvalidatedTokenRepository;
import com.smart_watering_system.SmartWateringSystem.repository.OtpRepository;
import com.smart_watering_system.SmartWateringSystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenService {

    InvalidatedTokenRepository invalidatedTokenRepository;
    OtpRepository otpRepository;
    UserRepository userRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    long validDuration;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    long refreshableDuration;

    public LoginResponse refreshToken(String token) throws ParseException, JOSEException {
        var signedToken = verifyToken(token, true);

        deleteToken(signedToken);

        var username = signedToken.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return LoginResponse.builder()
                .token(generateToken(user))
                .build();
    }

    public IntrospectResponse introspect(String token) throws ParseException, JOSEException {
        verifyToken(token, false);

        return IntrospectResponse.builder()
                .isAuthenticate(true)
                .build();
    }

    SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expirationTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant()
                .plus(refreshableDuration, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var isValid = signedJWT.verify(verifier);

        if (!isValid || invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.INVALID_TOKEN);

        if (!expirationTime.after(new Date()))
            throw new AppException(ErrorCode.EXPIRED_TOKEN);

        return signedJWT;
    }

    String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .claim("email", user.getEmail())
                .issuer("smart-watering")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(validDuration, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
        } catch (JOSEException e) {
            log.error("Cannot create token: ", e);
            throw new RuntimeException(e);
        }

        return jwsObject.serialize();
    }

    void deleteToken(SignedJWT signedToken) throws ParseException {
        String jit = signedToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = new Date(signedToken.getJWTClaimsSet().getIssueTime().toInstant()
                .plus(refreshableDuration, ChronoUnit.SECONDS).toEpochMilli());

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);
    }

    @Transactional
    public String generateOtp(String email) {
        String otpCode = String.valueOf(new Random().nextInt(900000) + 100000);
        LocalDateTime expiredTime = LocalDateTime.now().plusMinutes(5);

        otpRepository.deleteByEmail(email);

        Otp otp = Otp.builder()
                .email(email)
                .expiredTime(expiredTime)
                .build();

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        otp.setCode(passwordEncoder.encode(otpCode));

        otpRepository.save(otp);

        return otpCode;
    }

    public void verifyOtp(VerifyRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.WRONG_EMAIL));

        Otp otp = otpRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EXPIRED_OTP));

        if(LocalDateTime.now().isAfter(otp.getExpiredTime())) throw new AppException(ErrorCode.EXPIRED_OTP);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean isValidOtp = passwordEncoder.matches(request.getCode(), otp.getCode());
        if(!isValidOtp) throw new AppException(ErrorCode.WRONG_OTP);

        if(!user.isVerified()){
            user.setVerified(true);
            userRepository.save(user);
        }

        otpRepository.delete(otp);
    }

}
