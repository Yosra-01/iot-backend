package com.dxc.iotmonitor.sensor.traffic.dto;

import com.dxc.iotmonitor.enums.CongestionLevel;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrafficSensorRequest {

    @NotBlank
    private String location;

    @NotNull
    @PastOrPresent
    private LocalDateTime timestamp;

    @NotNull
    @Min(0)
    @Max(500)
    private Integer trafficDensity;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("120.0")
    private Float avgSpeed;

    @NotNull
    private CongestionLevel congestionLevel;
}
