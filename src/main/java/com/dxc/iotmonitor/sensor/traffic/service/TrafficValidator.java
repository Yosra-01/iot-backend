package com.dxc.iotmonitor.sensor.traffic.service;

import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.sensor.SensorLocations;
import com.dxc.iotmonitor.sensor.common.SensorValidator;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class TrafficValidator implements SensorValidator<TrafficSensorRequest> {

    private static final String VALIDATION_FAILED_LOG = "[TrafficValidator][validate] validation failed: {}";

    @Override
    public void validate(TrafficSensorRequest request) {
        if (request.getLocation() == null || request.getLocation().isBlank()) {
            String message = "location is required";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
        if (!SensorLocations.isValid(SensorType.TRAFFIC, request.getLocation())) {
            String message = "invalid location for this sensor type";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTimestamp() == null) {
            String message = "timestamp is required";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTimestamp().isAfter(LocalDateTime.now())) {
            String message = "timestamp must not be in the future";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTrafficDensity() == null || request.getTrafficDensity() < 0 || request.getTrafficDensity() > 500) {
            String message = "trafficDensity must be between 0 and 500";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
        if (request.getAvgSpeed() == null || request.getAvgSpeed() < 0.0f || request.getAvgSpeed() > 120.0f) {
            String message = "avgSpeed must be between 0 and 120";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
        if (request.getCongestionLevel() == null) {
            String message = "congestionLevel is required";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
    }
}
