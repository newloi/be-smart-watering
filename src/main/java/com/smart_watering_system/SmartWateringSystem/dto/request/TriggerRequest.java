package com.smart_watering_system.SmartWateringSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TriggerRequest {
    @NotNull(message = "NOT_BLANK")
    boolean status;
}
