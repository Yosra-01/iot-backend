package com.dxc.iotmonitor.alert.dto;

import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;

import java.time.LocalDateTime;

public record AlertFilterParams(
        SensorType sensorType,
        Metric metric,
        AlertType alertType,
        String location,
        LocalDateTime triggeredStart,
        LocalDateTime triggeredEnd,
        Boolean read
) {}
