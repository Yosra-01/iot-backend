package com.dxc.iotmonitor.sensor.airpollution.dto;

import com.dxc.iotmonitor.enums.PollutionLevel;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class AirPollutionSensorResponse {

    private UUID id;
    private String location;
    private LocalDateTime timestamp;
    @JsonProperty("pm2_5")
    private Float pm25;
    private Float pm10;
    private Float co;
    private Float no2;
    private Float so2;
    private Float ozone;
    private PollutionLevel pollutionLevel;
}
