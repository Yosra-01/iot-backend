package com.dxc.iotmonitor.sensor.traffic.dto;

import com.dxc.iotmonitor.enums.CongestionLevel;
import com.dxc.iotmonitor.enums.TrafficLocation;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrafficSensorRequest {

    @NotNull(message = "location is required")
    private TrafficLocation location;

    @NotNull(message = "timestamp is required")
    @PastOrPresent(message = "timestamp must not be in the future")
    private LocalDateTime timestamp;

    @NotNull(message = "trafficDensity is required")
    @Min(value = 0, message = "trafficDensity must be between 0 and 500")
    @Max(value = 500, message = "trafficDensity must be between 0 and 500")
    private Integer trafficDensity;

    @NotNull(message = "avgSpeed is required")
    @DecimalMin(value = "0.0", message = "avgSpeed must be between 0 and 120")
    @DecimalMax(value = "120.0", message = "avgSpeed must be between 0 and 120")
    private Float avgSpeed;

    @NotNull(message = "congestionLevel is required")
    private CongestionLevel congestionLevel;
}
