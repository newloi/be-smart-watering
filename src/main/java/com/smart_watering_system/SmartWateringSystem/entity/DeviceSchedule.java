package com.smart_watering_system.SmartWateringSystem.entity;

import com.smart_watering_system.SmartWateringSystem.enums.Day;
import com.smart_watering_system.SmartWateringSystem.enums.Repeat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeviceSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    LocalTime startTime;
    Date dateOneTime;
    long duration;
    Repeat repeatType;
    List<Day> daysOfWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    Device device;
}
