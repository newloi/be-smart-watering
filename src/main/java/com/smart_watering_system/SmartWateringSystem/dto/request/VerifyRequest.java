package com.smart_watering_system.SmartWateringSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifyRequest {
    @NotBlank(message = "NOT_BLANK")
    String email;

    @NotBlank(message = "NOT_BLANK")
    @Size(min  = 6, max = 6, message = "INVALID_OTP")
    String code;
}
