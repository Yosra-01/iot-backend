package com.dxc.iotmonitor.sensor.airpollution.dto;

import com.dxc.iotmonitor.enums.PollutionLevel;
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
public class AirPollutionStatsResponse {

    private LocalDateTime from;
    private LocalDateTime to;
    private String location;
    private Long totalReadings;
    private Double avgCo;
    private Double avgOzone;
    private Float maxCo;
    private Float minCo;
    private Float maxOzone;
    private Float minOzone;
    private Long alertsTriggered;
    private Map<PollutionLevel, Long> pollutionLevelDistribution;
    private List<DailyAverage> dailyAverages;

    public record DailyAverage(
            String date,
            Double avgCo,
            Double avgOzone
    ) {}
}
