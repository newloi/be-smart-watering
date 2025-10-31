package com.smart_watering_system.SmartWateringSystem.configuration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.smart_watering_system.SmartWateringSystem.enums.ErrorCode;
import com.smart_watering_system.SmartWateringSystem.exception.AppException;
import com.smart_watering_system.SmartWateringSystem.service.TokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthChannelInterceptor implements ChannelInterceptor {

    TokenService tokenService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if(StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if(authHeader == null || !authHeader.startsWith("Bearer ")) throw new MessagingException("Missing Authorization header!");

            String token = authHeader.substring(7);
            try {
                SignedJWT signedJWT = tokenService.verifyToken(token);
                String username = signedJWT.getJWTClaimsSet().getSubject();
                Authentication auth = new UsernamePasswordAuthenticationToken(username, null, List.of());
                accessor.setUser(auth);
            } catch (AppException e) {
                throw new MessagingException(e.getErrorCode().getMessage());
            } catch (JOSEException | ParseException e) {
                throw new MessagingException(ErrorCode.INVALID_TOKEN.getMessage());
            }
        }

        return message;
    }

}
