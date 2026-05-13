package com.dxc.iotmonitor.alert.mapper;

import com.dxc.iotmonitor.alert.AlertData;
import com.dxc.iotmonitor.alert.dto.response.AlertResponse;
import org.springframework.stereotype.Component;

@Component
public class AlertMapper {

    public AlertResponse toResponse(AlertData alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .sensorType(alert.getSensorType())
                .location(alert.getLocation())
                .metric(alert.getMetric())
                .triggeredValue(alert.getTriggeredValue())
                .thresholdValue(alert.getThresholdValue())
                .alertType(alert.getAlertType())
                .triggeredAt(alert.getTriggeredAt())
                .build();
    }
}
