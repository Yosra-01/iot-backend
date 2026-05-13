package com.dxc.iotmonitor.sensor.streetlight.model;

import com.dxc.iotmonitor.enums.LightStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "street_light_sensor_data")
@Check(constraints = "brightness_level >= 0 AND brightness_level <= 100")
@Check(constraints = "power_consumption >= 0 AND power_consumption <= 5000")
@Getter
@Setter
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
