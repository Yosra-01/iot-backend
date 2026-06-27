package com.dxc.iotmonitor.sensor.airpollution.service;

import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.sensor.SensorLocations;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorRequest;
import com.dxc.iotmonitor.sensor.common.SensorValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class AirPollutionValidator implements SensorValidator<AirPollutionSensorRequest> {

    @Override
    public void validate(AirPollutionSensorRequest request) {
        if (request.getLocation() == null || request.getLocation().isBlank()) {
            String message = "location is required";
            log.warn("[AirPollutionValidator][validate] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (!SensorLocations.isValid(SensorType.AIR_POLLUTION, request.getLocation())) {
            String message = "invalid location for this sensor type";
            log.warn("[AirPollutionValidator][validate] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTimestamp() == null) {
            String message = "timestamp is required";
            log.warn("[AirPollutionValidator][validate] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTimestamp().isAfter(LocalDateTime.now())) {
            String message = "timestamp must not be in the future";
            log.warn("[AirPollutionValidator][validate] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getPm2_5() == null || request.getPm2_5() < 0 || request.getPm2_5() > 500) {
            String message = "pm2_5 must be between 0 and 500";
            log.warn("[AirPollutionValidator][validate] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getPm10() == null || request.getPm10() < 0 || request.getPm10() > 600) {
            String message = "pm10 must be between 0 and 600";
            log.warn("[AirPollutionValidator][validate] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getCo() == null || request.getCo() < 0 || request.getCo() > 50) {
            String message = "co must be between 0 and 50";
            log.warn("[AirPollutionValidator][validate] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getNo2() == null || request.getNo2() < 0 || request.getNo2() > 200) {
            String message = "no2 must be between 0 and 200";
            log.warn("[AirPollutionValidator][validate] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getSo2() == null || request.getSo2() < 0 || request.getSo2() > 350) {
            String message = "so2 must be between 0 and 350";
            log.warn("[AirPollutionValidator][validate] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getOzone() == null || request.getOzone() < 0 || request.getOzone() > 300) {
            String message = "ozone must be between 0 and 300";
            log.warn("[AirPollutionValidator][validate] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getPollutionLevel() == null) {
            String message = "pollutionLevel is required";
            log.warn("[AirPollutionValidator][validate] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
    }
}
