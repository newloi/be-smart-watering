package com.smart_watering_system.SmartWateringSystem.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "user_id"})
        }
)
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "deviceId", unique = true, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci")
    String deviceId;
    String name;
    String topicSensor;
    String topicWatering;
    boolean isWatering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", foreignKey = @ForeignKey(name = "fk_group",
                    foreignKeyDefinition = "FOREIGN KEY (group_id) REFERENCES group_table(id) ON DELETE SET NULL"))
    Group group;
}
