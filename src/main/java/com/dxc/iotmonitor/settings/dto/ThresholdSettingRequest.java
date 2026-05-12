package com.dxc.iotmonitor.settings.dto;

import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ThresholdSettingRequest {

    @NotNull(message = "type is required")
    private SensorType type;

    @NotNull(message = "metric is required")
    private Metric metric;

    @NotNull(message = "thresholdValue is required")
    private Float thresholdValue;

    @NotNull(message = "alertType is required")
    private AlertType alertType;
}
