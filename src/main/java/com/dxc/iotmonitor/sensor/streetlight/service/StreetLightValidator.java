package com.dxc.iotmonitor.sensor.streetlight.service;

import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.sensor.SensorLocations;
import com.dxc.iotmonitor.sensor.common.SensorValidator;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
public class StreetLightValidator implements SensorValidator<StreetLightSensorRequest> {

    private static final String VALIDATION_FAILED_LOG = "[StreetLightValidator][validate] validation failed: {}";

    @Override
    public void validate(StreetLightSensorRequest request) {
        if (request.getLocation() == null || request.getLocation().isBlank()) {
            String message = "location is required";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
        if (!SensorLocations.isValid(SensorType.STREET_LIGHT, request.getLocation())) {
            String message = "invalid location for this sensor type";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTimestamp() == null) {
            String message = "timestamp is required";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTimestamp().isAfter(LocalDateTime.now(ZoneId.of("Africa/Cairo")))) {
            String message = "timestamp must not be in the future";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
        if (request.getBrightnessLevel() == null || request.getBrightnessLevel() < 0 || request.getBrightnessLevel() > 100) {
            String message = "brightnessLevel must be between 0 and 100";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
        if (request.getPowerConsumption() == null || request.getPowerConsumption() < 0 || request.getPowerConsumption() > 5000) {
            String message = "powerConsumption must be between 0 and 5000";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
        if (request.getStatus() == null) {
            String message = "status is required";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
    }
}
