package com.dxc.iotmonitor.sensor.traffic.service;

import com.dxc.iotmonitor.alert.service.AlertService;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.sensor.SensorLocations;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorRequest;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorResponse;
import com.dxc.iotmonitor.sensor.traffic.mapper.TrafficSensorMapper;
import com.dxc.iotmonitor.sensor.traffic.model.TrafficSensorData;
import com.dxc.iotmonitor.sensor.traffic.repository.TrafficSensorRepository;
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
public class TrafficSensorService {

    private final TrafficSensorRepository trafficSensorRepository;
    private final TrafficSensorMapper trafficSensorMapper;
    private final AlertService alertService;
    private final UserRepository userRepository;

    public TrafficSensorResponse save(TrafficSensorRequest request, Optional<User> user) {
        if (request.getLocation() == null || request.getLocation().isBlank()) {
            String message = "location is required";
            log.warn("[TrafficSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (!SensorLocations.isValid(SensorType.TRAFFIC, request.getLocation())) {
            String message = "invalid location for this sensor type";
            log.warn("[TrafficSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTimestamp() == null) {
            String message = "timestamp is required";
            log.warn("[TrafficSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTimestamp().isAfter(LocalDateTime.now())) {
            String message = "timestamp must not be in the future";
            log.warn("[TrafficSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTrafficDensity() == null || request.getTrafficDensity() < 0 || request.getTrafficDensity() > 500) {
            String message = "trafficDensity must be between 0 and 500";
            log.warn("[TrafficSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getAvgSpeed() == null || request.getAvgSpeed() < 0.0f || request.getAvgSpeed() > 120.0f) {
            String message = "avgSpeed must be between 0 and 120";
            log.warn("[TrafficSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getCongestionLevel() == null) {
            String message = "congestionLevel is required";
            log.warn("[TrafficSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }

        log.info("[TrafficSensorService][save] saving traffic reading for location: {}", request.getLocation());

        TrafficSensorData entity = trafficSensorMapper.toEntity(request);
        TrafficSensorData savedEntity = trafficSensorRepository.save(entity);

        log.info("[TrafficSensorService][save] saved successfully with id: {}", savedEntity.getId());

        Map<Metric, Float> readings = new HashMap<>();
        readings.put(Metric.TRAFFIC_DENSITY, (float) savedEntity.getTrafficDensity());
        readings.put(Metric.AVG_SPEED, savedEntity.getAvgSpeed());
        if (user.isPresent()) {
            alertService.checkAndTrigger(SensorType.TRAFFIC, readings, savedEntity.getLocation(), user.get(), savedEntity.getId());
        } else {
            for (User u : userRepository.findAll()) {
                alertService.checkAndTrigger(SensorType.TRAFFIC, readings, savedEntity.getLocation(), u, savedEntity.getId());
            }
        }

        return trafficSensorMapper.toResponse(savedEntity);
    }

    public List<TrafficSensorResponse> getAll() {
        log.info("[TrafficSensorService][getAll] fetch started: scope=all");

        List<TrafficSensorData> entities = trafficSensorRepository.findAllByOrderByTimestampDesc();
        List<TrafficSensorResponse> responses = entities.stream()
                .map(trafficSensorMapper::toResponse)
                .toList();

        log.info("[TrafficSensorService][getAll] fetch completed: count={}", responses.size());

        return responses;
    }

    public TrafficSensorResponse getById(String id) {
        log.info("[TrafficSensorService][getById] Fetching record with id: {}", id);
        UUID uuid = UUID.fromString(id);
        TrafficSensorData entity = trafficSensorRepository.findById(uuid)
                .orElseThrow(() -> {
                    log.warn("[TrafficSensorService][getById] Not found: {}", id);
                    return new ResourceNotFoundException("Traffic sensor reading not found with id: " + id);
                });
        return trafficSensorMapper.toResponse(entity);
    }

    public TrafficSensorResponse getLatest() {
        TrafficSensorData entity = trafficSensorRepository.findTopByOrderByTimestampDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No traffic sensor readings found"));
        log.info("[TrafficSensorService][getLatest] Returning latest record");
        return trafficSensorMapper.toResponse(entity);
    }

    public void flush() {
        log.info("[TrafficSensorService][flush] flush started: scope=all");

        trafficSensorRepository.deleteAll();

        log.info("[TrafficSensorService][flush] flush completed: ok");
    }
}
