package com.smart_watering_system.SmartWateringSystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    @NotBlank(message = "NOT_BLANK")
    @Email(message = "INVALID_EMAIL")
    String email;

    @NotBlank(message = "NOT_BLANK")
    @Size(min = 8, message = "INVALID_PASSWORD")
    String newPassword;

    @NotBlank(message = "NOT_BLANK")
    @Size(min = 8, message = "INVALID_PASSWORD")
    String confirmNewPassword;

    @NotBlank(message = "NOT_BLANK")
    @Size(min  = 6, max = 6, message = "INVALID_OTP")
    String code;
}
