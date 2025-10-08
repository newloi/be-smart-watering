package com.smart_watering_system.SmartWateringSystem.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(
        name = "group_table",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "user_id"})
        }
)
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;
    boolean isWatering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @OneToMany(mappedBy = "group", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    List<Device> devices;
}
