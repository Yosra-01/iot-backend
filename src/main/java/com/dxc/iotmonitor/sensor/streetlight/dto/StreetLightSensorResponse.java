package com.dxc.iotmonitor.sensor.streetlight.dto;

import com.dxc.iotmonitor.enums.LightStatus;
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
public class StreetLightSensorResponse {

    private UUID id;
    private String location;
    private LocalDateTime timestamp;
    private Integer brightnessLevel;
    private Float powerConsumption;
    private LightStatus status;
}
