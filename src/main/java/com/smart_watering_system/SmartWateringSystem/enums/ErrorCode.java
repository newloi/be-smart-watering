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
    INVALID_USERNAME(
            HttpStatus.BAD_REQUEST.value(),
            "Username must be at least {min} characters",
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
    ),
    UNAUTHENTICATED(
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthenticated",
            HttpStatus.UNAUTHORIZED
    ),
    DEVICE_EXISTED(
            HttpStatus.CONFLICT.value(),
            "Device has been existed",
            HttpStatus.CONFLICT
    ),
    DEVICE_NOT_EXISTED(
            HttpStatus.NOT_FOUND.value(),
            "Device not existed",
            HttpStatus.NOT_FOUND
    ),
    GROUP_EXISTED(
            HttpStatus.CONFLICT.value(),
            "Group has been existed",
            HttpStatus.CONFLICT
    ),
    GROUP_NOT_EXISTED(
            HttpStatus.NOT_FOUND.value(),
            "Group not existed",
            HttpStatus.NOT_FOUND
    ),
    DEVICE_IS_RUNNING(
            HttpStatus.CONFLICT.value(),
            "Device is running",
            HttpStatus.CONFLICT
    ),
    INVALID_ACTION(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid action",
            HttpStatus.BAD_REQUEST
    ),
    DEVICE_STOPPED(
            HttpStatus.BAD_REQUEST.value(),
            "Device isn't running",
            HttpStatus.BAD_REQUEST
    ),
    GROUP_STOPPED(
            HttpStatus.BAD_REQUEST.value(),
            "Group isn't running",
            HttpStatus.BAD_REQUEST
    )

    ;

    int statusCode;
    String message;
    HttpStatus status;
}
