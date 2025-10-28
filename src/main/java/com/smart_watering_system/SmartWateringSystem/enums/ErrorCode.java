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
    ),EMAIL_USED(
            HttpStatus.CONFLICT.value(),
            "Email is already in use",
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
    ),INVALID_EMAIL(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid email",
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
    SCHEDULE_NOT_EXISTED(
            HttpStatus.NOT_FOUND.value(),
            "Schedule not existed",
            HttpStatus.NOT_FOUND
    ),
    NOT_BLANK(
            HttpStatus.BAD_REQUEST.value(),
            "Please send all fields",
            HttpStatus.BAD_REQUEST
    ),
    ACC_NOT_VERIFIED(
            HttpStatus.UNAUTHORIZED.value(),
            "Account has not been verified",
            HttpStatus.UNAUTHORIZED
    ),
    INVALID_OTP(
            HttpStatus.BAD_REQUEST.value(),
            "OTP must be {min} characters",
    HttpStatus.BAD_REQUEST
    ),
    WRONG_OTP(
            HttpStatus.UNAUTHORIZED.value(),
            "OTP is wrong",
            HttpStatus.UNAUTHORIZED
    ),
    EXPIRED_OTP(
            HttpStatus.UNAUTHORIZED.value(),
            "OTP has expired",
            HttpStatus.UNAUTHORIZED
    ),
    WRONG_EMAIL(
            HttpStatus.UNAUTHORIZED.value(),
            "Email is wrong",
            HttpStatus.UNAUTHORIZED
    ),
    PASS_NOT_MATCH(
            HttpStatus.BAD_REQUEST.value(),
            "New password and confirm password must match",
            HttpStatus.BAD_REQUEST
    ),
    TOO_MANY_REQUESTS(
            HttpStatus.TOO_MANY_REQUESTS.value(),
            "Too many requests. Please try again later.",
            HttpStatus.TOO_MANY_REQUESTS
    )
    ;

    int statusCode;
    String message;
    HttpStatus status;
}
