package com.dxc.iotmonitor.sensor.streetlight.dto;

import com.dxc.iotmonitor.enums.LightStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreetLightStatsResponse {

    private LocalDateTime from;
    private LocalDateTime to;
    private String location;
    private Long totalReadings;
    private Double avgBrightness;
    private Double avgPowerConsumption;
    private Integer maxBrightness;
    private Integer minBrightness;
    private Float maxPowerConsumption;
    private Float minPowerConsumption;
    private Long alertsTriggered;
    private Map<LightStatus, Long> statusDistribution;
    private List<DailyAverage> dailyAverages;

    public record DailyAverage(
            String date,
            Double avgBrightness,
            Double avgPowerConsumption
    ) {}
}
