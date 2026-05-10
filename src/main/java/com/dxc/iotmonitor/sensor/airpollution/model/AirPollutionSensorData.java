package com.dxc.iotmonitor.sensor.airpollution.model;

import com.dxc.iotmonitor.enums.PollutionLevel;
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
@Table(name = "air_pollution_sensor_data")
@Check(constraints = "pm2_5 >= 0 AND pm2_5 <= 500")
@Check(constraints = "pm10 >= 0 AND pm10 <= 600")
@Check(constraints = "co >= 0 AND co <= 50")
@Check(constraints = "no2 >= 0 AND no2 <= 200")
@Check(constraints = "so2 >= 0 AND so2 <= 350")
@Check(constraints = "ozone >= 0 AND ozone <= 300")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirPollutionSensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
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
