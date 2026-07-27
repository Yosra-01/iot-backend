package com.dxc.iotmonitor.sensor.airpollution.dto;

import com.dxc.iotmonitor.enums.PollutionLevel;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class AirPollutionSensorRequest {

    @NotBlank(message = "location is required")
    private String location;

    @NotNull(message = "timestamp is required")
    @PastOrPresent(message = "timestamp must not be in the future")
    private LocalDateTime timestamp;

    @NotNull(message = "pm2_5 is required")
    @DecimalMin(value = "0.0", message = "pm2_5 must be between 0 and 500")
    @DecimalMax(value = "500.0", message = "pm2_5 must be between 0 and 500")
    @JsonProperty("pm2_5")
    private Float pm25;

    @NotNull(message = "pm10 is required")
    @DecimalMin(value = "0.0", message = "pm10 must be between 0 and 600")
    @DecimalMax(value = "600.0", message = "pm10 must be between 0 and 600")
    private Float pm10;

    @NotNull(message = "co is required")
    @DecimalMin(value = "0.0", message = "co must be between 0 and 50")
    @DecimalMax(value = "50.0", message = "co must be between 0 and 50")
    private Float co;

    @NotNull(message = "no2 is required")
    @DecimalMin(value = "0.0", message = "no2 must be between 0 and 200")
    @DecimalMax(value = "200.0", message = "no2 must be between 0 and 200")
    private Float no2;

    @NotNull(message = "so2 is required")
    @DecimalMin(value = "0.0", message = "so2 must be between 0 and 350")
    @DecimalMax(value = "350.0", message = "so2 must be between 0 and 350")
    private Float so2;

    @NotNull(message = "ozone is required")
    @DecimalMin(value = "0.0", message = "ozone must be between 0 and 300")
    @DecimalMax(value = "300.0", message = "ozone must be between 0 and 300")
    private Float ozone;

    @NotNull(message = "pollutionLevel is required")
    private PollutionLevel pollutionLevel;
}
