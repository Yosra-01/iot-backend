package com.dxc.iotmonitor.sensor.airpollution.service;

import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.sensor.SensorLocations;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorRequest;
import com.dxc.iotmonitor.sensor.common.SensorValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
public class AirPollutionValidator implements SensorValidator<AirPollutionSensorRequest> {

    private static final String VALIDATION_FAILED_LOG = "[AirPollutionValidator][validate] validation failed: {}";

    @Override
    public void validate(AirPollutionSensorRequest request) {
        validateLocation(request);
        validateTimestamp(request);
        validateRange(request.getPm25(), 0, 500, "pm2_5");
        validateRange(request.getPm10(), 0, 600, "pm10");
        validateRange(request.getCo(), 0, 50, "co");
        validateRange(request.getNo2(), 0, 200, "no2");
        validateRange(request.getSo2(), 0, 350, "so2");
        validateRange(request.getOzone(), 0, 300, "ozone");
        validatePollutionLevel(request);
    }

    private void validateLocation(AirPollutionSensorRequest request) {
        if (request.getLocation() == null || request.getLocation().isBlank()) {
            String message = "location is required";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
        if (!SensorLocations.isValid(SensorType.AIR_POLLUTION, request.getLocation())) {
            String message = "invalid location for this sensor type";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
    }

    private void validateTimestamp(AirPollutionSensorRequest request) {
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
    }

    private void validateRange(Float value, float min, float max, String fieldName) {
        if (value == null || value < min || value > max) {
            String message = fieldName + " must be between " + (int) min + " and " + (int) max;
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
    }

    private void validatePollutionLevel(AirPollutionSensorRequest request) {
        if (request.getPollutionLevel() == null) {
            String message = "pollutionLevel is required";
            log.warn(VALIDATION_FAILED_LOG, message);
            throw new IllegalArgumentException(message);
        }
    }
}
