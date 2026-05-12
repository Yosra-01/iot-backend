package com.dxc.iotmonitor.settings.dto;

import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ThresholdSettingResponse {

    private UUID id;
    private SensorType type;
    private Metric metric;
    private Float thresholdValue;
    private AlertType alertType;
    private LocalDateTime createdAt;
}
