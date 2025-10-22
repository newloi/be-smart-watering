package com.smart_watering_system.SmartWateringSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {
    @NotBlank(message = "NOT_BLANK")
    String username;

    @NotBlank(message = "NOT_BLANK")
    String password;
}
