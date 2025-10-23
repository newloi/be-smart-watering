package com.smart_watering_system.SmartWateringSystem.dto.request;

import com.smart_watering_system.SmartWateringSystem.entity.Device;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupRequest {
    @NotBlank(message = "NOT_BLANK")
    String name;

    @NotNull(message = "NOT_BLANK")
    List<String> devices;
}
