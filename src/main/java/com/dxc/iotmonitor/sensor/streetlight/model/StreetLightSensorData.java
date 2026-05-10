package com.dxc.iotmonitor.sensor.streetlight.model;

import com.dxc.iotmonitor.enums.LightStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "street_light_sensor_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreetLightSensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Integer brightnessLevel;

    @Column(nullable = false)
    private Float powerConsumption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LightStatus status;
}
