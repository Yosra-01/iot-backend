package com.dxc.iotmonitor.sensor.airpollution.model;

import com.dxc.iotmonitor.enums.PollutionLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "air_pollution_sensor_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirPollutionSensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "pm2_5", nullable = false)
    private Float pm2_5;

    @Column(nullable = false)
    private Float pm10;

    @Column(nullable = false)
    private Float co;

    @Column(nullable = false)
    private Float no2;

    @Column(nullable = false)
    private Float so2;

    @Column(nullable = false)
    private Float ozone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PollutionLevel pollutionLevel;
}
