package com.dxc.iotmonitor.sensor.traffic.dto;

import com.dxc.iotmonitor.enums.CongestionLevel;
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
public class TrafficStatsResponse {

    private LocalDateTime from;
    private LocalDateTime to;
    private String location;
    private Long totalReadings;
    private Double avgTrafficDensity;
    private Double avgSpeed;
    private Integer maxTrafficDensity;
    private Integer minTrafficDensity;
    private Float maxSpeed;
    private Float minSpeed;
    private Long alertsTriggered;
    private Map<CongestionLevel, Long> congestionLevelDistribution;
    private List<DailyAverage> dailyAverages;

    public record DailyAverage(
            String date,
            Double avgTrafficDensity,
            Double avgSpeed
    ) {}
}
