package com.dxc.iotmonitor.sensor.airpollution.dto;

import com.dxc.iotmonitor.enums.AirPollutionLocation;
import com.dxc.iotmonitor.enums.PollutionLevel;
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
    private AirPollutionLocation location;
    private LocalDateTime timestamp;
    private Float pm2_5;
    private Float pm10;
    private Float co;
    private Float no2;
    private Float so2;
    private Float ozone;
    private PollutionLevel pollutionLevel;
}
