package com.dxc.iotmonitor.sensor.traffic.dto;

import com.dxc.iotmonitor.enums.CongestionLevel;

import java.time.LocalDateTime;

public record TrafficFilterParams(
        String location,
        Integer minDensity,
        Integer maxDensity,
        Float minSpeed,
        Float maxSpeed,
        CongestionLevel congestionLevel,
        LocalDateTime timestampStart,
        LocalDateTime timestampEnd
) {}
