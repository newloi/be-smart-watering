package com.smart_watering_system.SmartWateringSystem.configuration;

import com.nimbusds.jose.JOSEException;
import com.smart_watering_system.SmartWateringSystem.service.TokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomJwtDecoder implements JwtDecoder {

    @NonFinal
    @Value("${jwt.signerKey}")
    String SIGNER_KEY;

    TokenService tokenService;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            tokenService.introspect(token);
        } catch (AppException exception) {
            throw new InvalidBearerTokenException(exception.getErrorCode().name(), exception);
        } catch (ParseException | JOSEException ignored) {}

        SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");

        var nimbusJwtDecoder = NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();

        return nimbusJwtDecoder.decode(token);
    }

}
