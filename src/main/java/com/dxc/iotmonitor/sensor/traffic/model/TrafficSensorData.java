package com.dxc.iotmonitor.sensor.traffic.model;

import com.dxc.iotmonitor.enums.CongestionLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "traffic_sensors_data", indexes = @Index(name = "idx_traffic_loc_ts", columnList = "location,timestamp"))
@Check(constraints = "traffic_density >= 0 AND traffic_density <= 500")
@Check(constraints = "avg_speed >= 0 AND avg_speed <= 120")
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
    private Integer trafficDensity;

    @Column(nullable = false)
    private Float avgSpeed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CongestionLevel congestionLevel;
}