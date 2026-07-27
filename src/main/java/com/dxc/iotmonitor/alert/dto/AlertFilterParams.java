package com.dxc.iotmonitor.alert.dto;

import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.CongestionLevel;
import com.dxc.iotmonitor.enums.LightStatus;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.PollutionLevel;
import com.dxc.iotmonitor.enums.SensorType;

import java.time.LocalDateTime;

public record AlertFilterParams(
        SensorType sensorType,
        Metric metric,
        AlertType alertType,
        String location,
        LocalDateTime triggeredStart,
        LocalDateTime triggeredEnd,
        Boolean read,
        PollutionLevel pollutionLevel,
        CongestionLevel congestionLevel,
        LightStatus status
) {
    public AlertFilterParams(SensorType sensorType, Metric metric, AlertType alertType, String location,
                             LocalDateTime triggeredStart, LocalDateTime triggeredEnd, Boolean read) {
        this(sensorType, metric, alertType, location, triggeredStart, triggeredEnd, read, null, null, null);
    }
}
