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
public class UserRequest {
    @NotBlank(message = "NOT_BLANK")
    @Size(min = 3, message = "INVALID_USERNAME")
    String username;

    @NotBlank(message = "NOT_BLANK")
    @Size(min = 8, message = "INVALID_PASSWORD")
    String password;

    @NotBlank(message = "NOT_BLANK")
    @Email(message = "INVALID_EMAIL")
    String email;
}
