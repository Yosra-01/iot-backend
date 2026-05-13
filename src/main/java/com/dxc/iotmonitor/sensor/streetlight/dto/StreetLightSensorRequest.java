package com.dxc.iotmonitor.sensor.streetlight.dto;

import com.dxc.iotmonitor.enums.LightStatus;
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
public class StreetLightSensorRequest {

    @NotBlank(message = "location is required")
    private String location;

    @NotNull(message = "timestamp is required")
    @PastOrPresent(message = "timestamp must not be in the future")
    private LocalDateTime timestamp;

    @NotNull(message = "brightnessLevel is required")
    @Min(value = 0, message = "brightnessLevel must be between 0 and 100")
    @Max(value = 100, message = "brightnessLevel must be between 0 and 100")
    private Integer brightnessLevel;

    @NotNull(message = "powerConsumption is required")
    @DecimalMin(value = "0.0", message = "powerConsumption must be between 0 and 5000")
    @DecimalMax(value = "5000.0", message = "powerConsumption must be between 0 and 5000")
    private Float powerConsumption;

    @NotNull(message = "status is required")
    private LightStatus status;
}
