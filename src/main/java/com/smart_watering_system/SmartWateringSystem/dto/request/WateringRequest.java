package com.smart_watering_system.SmartWateringSystem.dto.request;

import com.smart_watering_system.SmartWateringSystem.enums.Action;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WateringRequest {
    @NotBlank(message = "NOT_BLANK")
    Action action;

    @NotBlank(message = "NOT_BLANK")
    long duration;
}
