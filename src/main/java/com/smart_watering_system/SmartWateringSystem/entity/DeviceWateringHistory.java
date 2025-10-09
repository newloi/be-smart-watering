package com.smart_watering_system.SmartWateringSystem.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeviceWateringHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    boolean byGroup;
    long duration;
    LocalDateTime startTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    Device device;
}
