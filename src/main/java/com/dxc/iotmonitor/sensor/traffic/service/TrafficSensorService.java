package com.dxc.iotmonitor.sensor.traffic.service;

import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorRequest;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorResponse;
import com.dxc.iotmonitor.sensor.traffic.mapper.TrafficSensorMapper;
import com.dxc.iotmonitor.sensor.traffic.model.TrafficSensorData;
import com.dxc.iotmonitor.sensor.traffic.repository.TrafficSensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class TrafficSensorService {

    private final TrafficSensorRepository trafficSensorRepository;
    private final TrafficSensorMapper trafficSensorMapper;

    public TrafficSensorResponse save(TrafficSensorRequest request) {
        if (request.getLocation() == null || request.getLocation().isBlank()) {
            String message = "location is required";
            log.warn("[TrafficSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getLocation().length() > 255) {
            String message = "location must not exceed 255 characters";
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

    public void flush() {
        log.info("[TrafficSensorService][flush] flush started: scope=all");

        trafficSensorRepository.deleteAll();

        log.info("[TrafficSensorService][flush] flush completed: ok");
    }
}
