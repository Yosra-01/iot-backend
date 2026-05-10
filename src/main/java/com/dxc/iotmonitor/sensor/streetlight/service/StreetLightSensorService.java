package com.dxc.iotmonitor.sensor.streetlight.service;

import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorRequest;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorResponse;
import com.dxc.iotmonitor.sensor.streetlight.mapper.StreetLightSensorMapper;
import com.dxc.iotmonitor.sensor.streetlight.model.StreetLightSensorData;
import com.dxc.iotmonitor.sensor.streetlight.repository.StreetLightSensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class StreetLightSensorService {
    private final StreetLightSensorRepository streetLightSensorRepository;
    private final StreetLightSensorMapper streetLightSensorMapper;

    public StreetLightSensorResponse save(StreetLightSensorRequest request) {
        if (request.getLocation() == null || request.getLocation().isBlank()) {
            String message = "location is required";
            log.warn("[StreetLightSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getLocation().length() > 255) {
            String message = "location must not exceed 255 characters";
            log.warn("[StreetLightSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTimestamp() == null) {
            String message = "timestamp is required";
            log.warn("[StreetLightSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTimestamp().isAfter(LocalDateTime.now())) {
            String message = "timestamp must not be in the future";
            log.warn("[StreetLightSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getBrightnessLevel() == null || request.getBrightnessLevel() < 0 || request.getBrightnessLevel() > 100) {
            String message = "brightnessLevel must be between 0 and 100";
            log.warn("[StreetLightSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getPowerConsumption() == null || request.getPowerConsumption() < 0 || request.getPowerConsumption() > 5000) {
            String message = "powerConsumption must be between 0 and 5000";
            log.warn("[StreetLightSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getStatus() == null) {
            String message = "status is required";
            log.warn("[StreetLightSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }

        log.info("[StreetLightSensorService][save] saving street light reading for location: {}", request.getLocation());

        StreetLightSensorData entity = streetLightSensorMapper.toEntity(request);
        StreetLightSensorData savedEntity = streetLightSensorRepository.save(entity);

        log.info("[StreetLightSensorService][save] saved successfully with id: {}", savedEntity.getId());

        return streetLightSensorMapper.toResponse(savedEntity);
    }

    public List<StreetLightSensorResponse> getAll() {
        log.info("[StreetLightSensorService][getAll] fetch started: scope=all");
        List<StreetLightSensorData> entities = streetLightSensorRepository.findAllByOrderByTimestampDesc();
        List<StreetLightSensorResponse> responses = entities.stream()
                .map(streetLightSensorMapper::toResponse)
                .toList();
        log.info("[StreetLightSensorService][getAll] fetch completed: count={}", responses.size());
        return responses;
    }

    public void flush() {
        log.info("[StreetLightSensorService][flush] flush started: scope=all");
        streetLightSensorRepository.deleteAll();
        log.info("[StreetLightSensorService][flush] flush completed: ok");
    }
}
