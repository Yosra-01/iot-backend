package com.dxc.iotmonitor.sensor.airpollution.dto;

import com.dxc.iotmonitor.enums.PollutionLevel;

import java.time.LocalDateTime;

public record AirPollutionFilterParams(
        String location,
        Float minPm25,
        Float maxPm25,
        Float minPm10,
        Float maxPm10,
        Float minCo,
        Float maxCo,
        Float minNo2,
        Float maxNo2,
        Float minSo2,
        Float maxSo2,
        Float minOzone,
        Float maxOzone,
        PollutionLevel pollutionLevel,
        LocalDateTime timestampStart,
        LocalDateTime timestampEnd
) {}
