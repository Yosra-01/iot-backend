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
            log.warn("[TrafficSensorService][save] Validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTimestamp() == null) {
            String message = "timestamp is required";
            log.warn("[TrafficSensorService][save] Validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTimestamp().isAfter(LocalDateTime.now())) {
            String message = "timestamp must not be in the future";
            log.warn("[TrafficSensorService][save] Validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTrafficDensity() == null || request.getTrafficDensity() < 0 || request.getTrafficDensity() > 500) {
            String message = "trafficDensity must be between 0 and 500";
            log.warn("[TrafficSensorService][save] Validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getAvgSpeed() == null || request.getAvgSpeed() < 0.0f || request.getAvgSpeed() > 120.0f) {
            String message = "avgSpeed must be between 0 and 120";
            log.warn("[TrafficSensorService][save] Validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getCongestionLevel() == null) {
            String message = "congestionLevel is required";
            log.warn("[TrafficSensorService][save] Validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }

        TrafficSensorData entity = trafficSensorMapper.toEntity(request);
        TrafficSensorData savedEntity = trafficSensorRepository.save(entity);
        log.info("Traffic sensor data saved successfully for location: {}", request.getLocation());
        return trafficSensorMapper.toResponse(savedEntity);
    }

    public List<TrafficSensorResponse> getAll() {
        List<TrafficSensorData> entities = trafficSensorRepository.findAllByOrderByTimestampDesc();
        return entities.stream()
                .map(trafficSensorMapper::toResponse)
                .toList();
    }

    public void flush() {
        trafficSensorRepository.deleteAll();
        log.info("[TrafficSensorService][flush] Traffic sensor table flushed successfully");
    }
}
