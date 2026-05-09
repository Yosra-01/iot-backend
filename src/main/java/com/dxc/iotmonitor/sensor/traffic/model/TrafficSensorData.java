package com.dxc.iotmonitor.sensor.traffic.model;

import com.dxc.iotmonitor.enums.CongestionLevel;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "traffic_sensors_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrafficSensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private int trafficDensity;

    @Column(nullable = false)
    private float avgSpeed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CongestionLevel congestionLevel;
}