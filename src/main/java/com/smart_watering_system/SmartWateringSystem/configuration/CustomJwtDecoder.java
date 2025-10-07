package com.smart_watering_system.SmartWateringSystem.configuration;

import com.nimbusds.jose.JOSEException;
import com.smart_watering_system.SmartWateringSystem.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Autowired
    private AuthService authService;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            authService.introspect(token);
        } catch (ParseException | JOSEException e) {
            throw new JwtException(e.getMessage());
        }

        SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");

        var nimbusJwtDecoder = NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();

        return nimbusJwtDecoder.decode(token);
    }

}
