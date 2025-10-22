package com.smart_watering_system.SmartWateringSystem.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponse {
    String accessToken;

    @JsonIgnore
    String refreshToken;
}
