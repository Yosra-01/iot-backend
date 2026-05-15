package com.dxc.iotmonitor.alert.dto.response;

import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertResponse {

    private UUID id;
    private SensorType sensorType;
    private String location;
    private Metric metric;
    private Float triggeredValue;
    private Float thresholdValue;
    private AlertType alertType;
    private LocalDateTime triggeredAt;
    private UUID readingId;
}
