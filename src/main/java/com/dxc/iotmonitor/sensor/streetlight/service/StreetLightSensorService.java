package com.dxc.iotmonitor.sensor.streetlight.service;

import com.dxc.iotmonitor.alert.service.AlertService;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.sensor.SensorLocations;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorRequest;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorResponse;
import com.dxc.iotmonitor.sensor.streetlight.mapper.StreetLightSensorMapper;
import com.dxc.iotmonitor.sensor.streetlight.model.StreetLightSensorData;
import com.dxc.iotmonitor.sensor.streetlight.repository.StreetLightSensorRepository;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class StreetLightSensorService {
    private final StreetLightSensorRepository streetLightSensorRepository;
    private final StreetLightSensorMapper streetLightSensorMapper;
    private final AlertService alertService;
    private final UserRepository userRepository;

    public StreetLightSensorResponse save(StreetLightSensorRequest request, Optional<User> user) {
        if (request.getLocation() == null || request.getLocation().isBlank()) {
            String message = "location is required";
            log.warn("[StreetLightSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (!SensorLocations.isValid(SensorType.STREET_LIGHT, request.getLocation())) {
            String message = "invalid location for this sensor type";
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

        Map<Metric, Float> readings = new HashMap<>();
        readings.put(Metric.BRIGHTNESS_LEVEL, (float) savedEntity.getBrightnessLevel());
        readings.put(Metric.POWER_CONSUMPTION, savedEntity.getPowerConsumption());
        if (user.isPresent()) {
            alertService.checkAndTrigger(SensorType.STREET_LIGHT, readings, savedEntity.getLocation(), user.get(), savedEntity.getId());
        } else {
            for (User u : userRepository.findAll()) {
                alertService.checkAndTrigger(SensorType.STREET_LIGHT, readings, savedEntity.getLocation(), u, savedEntity.getId());
            }
        }

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

    public StreetLightSensorResponse getById(String id) {
        log.info("[StreetLightSensorService][getById] Fetching record with id: {}", id);
        UUID uuid = UUID.fromString(id);
        StreetLightSensorData entity = streetLightSensorRepository.findById(uuid)
                .orElseThrow(() -> {
                    log.warn("[StreetLightSensorService][getById] Not found: {}", id);
                    return new ResourceNotFoundException("Street light sensor reading not found with id: " + id);
                });
        return streetLightSensorMapper.toResponse(entity);
    }

    public StreetLightSensorResponse getLatest() {
        StreetLightSensorData entity = streetLightSensorRepository.findTopByOrderByTimestampDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No street light sensor readings found"));
        log.info("[StreetLightSensorService][getLatest] Returning latest record");
        return streetLightSensorMapper.toResponse(entity);
    }

    public void flush() {
        log.info("[StreetLightSensorService][flush] flush started: scope=all");
        streetLightSensorRepository.deleteAll();
        log.info("[StreetLightSensorService][flush] flush completed: ok");
    }
}
