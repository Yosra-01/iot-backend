package com.dxc.iotmonitor.sensor.streetlight.dto;

import com.dxc.iotmonitor.enums.LightStatus;

import java.time.LocalDateTime;

public record StreetLightFilterParams(
        String location,
        Integer minBrightness,
        Integer maxBrightness,
        Float minPower,
        Float maxPower,
        LightStatus status,
        LocalDateTime timestampStart,
        LocalDateTime timestampEnd
) {}
