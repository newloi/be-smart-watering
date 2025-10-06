package com.smart_watering_system.SmartWateringSystem.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    USER_EXISTED(
            HttpStatus.CONFLICT.value(),
            "User has been existed",
            HttpStatus.CONFLICT
    ),
    USER_NOT_EXISTED(
            HttpStatus.NOT_FOUND.value(),
            "User not existed",
            HttpStatus.NOT_FOUND
    ),
    INVALID_PASSWORD(
            HttpStatus.BAD_REQUEST.value(),
            "Password must be at least {min} characters",
            HttpStatus.BAD_REQUEST
    ),
    WRONG_PASSWORD(
            HttpStatus.UNAUTHORIZED.value(),
            "Password is wrong",
            HttpStatus.UNAUTHORIZED
    ),
    INVALID_TOKEN(
            HttpStatus.UNAUTHORIZED.value(),
            "Invalid token",
            HttpStatus.UNAUTHORIZED
    ),
    EXPIRED_TOKEN(
            HttpStatus.UNAUTHORIZED.value(),
            "Token has expired",
            HttpStatus.UNAUTHORIZED
    )

    ;

    int statusCode;
    String message;
    HttpStatus status;
}
