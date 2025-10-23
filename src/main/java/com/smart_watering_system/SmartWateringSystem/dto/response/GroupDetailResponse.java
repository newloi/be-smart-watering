package com.smart_watering_system.SmartWateringSystem.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupDetailResponse {
    String id;
    String name;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<DeviceResponse> devices;
}
