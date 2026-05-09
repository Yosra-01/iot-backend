package com.dxc.iotmonitor.sensor.traffic.dto;

import com.dxc.iotmonitor.enums.CongestionLevel;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrafficSensorResponse {

    private UUID id;
    private CongestionLevel congestionLevel;
    private String location;
    private int trafficDensity;
    private float avgSpeed;
    private LocalDateTime timestamp;

}
