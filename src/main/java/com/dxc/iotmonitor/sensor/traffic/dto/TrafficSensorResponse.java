package com.dxc.iotmonitor.sensor.traffic.dto;

import com.dxc.iotmonitor.enums.CongestionLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrafficSensorResponse {

    private UUID id;
    private String location;
    private LocalDateTime timestamp;
    private Integer trafficDensity;
    private Float avgSpeed;
    private CongestionLevel congestionLevel;
}
