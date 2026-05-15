package com.dxc.iotmonitor.sensor.airpollution.service;

import com.dxc.iotmonitor.alert.service.AlertService;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorRequest;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorResponse;
import com.dxc.iotmonitor.sensor.airpollution.mapper.AirPollutionSensorMapper;
import com.dxc.iotmonitor.sensor.airpollution.model.AirPollutionSensorData;
import com.dxc.iotmonitor.sensor.airpollution.repository.AirPollutionSensorRepository;
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
public class AirPollutionSensorService {

    private final AirPollutionSensorRepository airPollutionSensorRepository;
    private final AirPollutionSensorMapper airPollutionSensorMapper;
    private final AlertService alertService;
    private final UserRepository userRepository;

    public AirPollutionSensorResponse save(AirPollutionSensorRequest request, Optional<User> user) {
        if (request.getLocation() == null || request.getLocation().isBlank()) {
            String message = "location is required";
            log.warn("[AirPollutionSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTimestamp() == null) {
            String message = "timestamp is required";
            log.warn("[AirPollutionSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getTimestamp().isAfter(LocalDateTime.now())) {
            String message = "timestamp must not be in the future";
            log.warn("[AirPollutionSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getPm2_5() == null || request.getPm2_5() < 0 || request.getPm2_5() > 500) {
            String message = "pm2_5 must be between 0 and 500";
            log.warn("[AirPollutionSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getPm10() == null || request.getPm10() < 0 || request.getPm10() > 600) {
            String message = "pm10 must be between 0 and 600";
            log.warn("[AirPollutionSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getCo() == null || request.getCo() < 0 || request.getCo() > 50) {
            String message = "co must be between 0 and 50";
            log.warn("[AirPollutionSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getNo2() == null || request.getNo2() < 0 || request.getNo2() > 200) {
            String message = "no2 must be between 0 and 200";
            log.warn("[AirPollutionSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getSo2() == null || request.getSo2() < 0 || request.getSo2() > 350) {
            String message = "so2 must be between 0 and 350";
            log.warn("[AirPollutionSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getOzone() == null || request.getOzone() < 0 || request.getOzone() > 300) {
            String message = "ozone must be between 0 and 300";
            log.warn("[AirPollutionSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (request.getPollutionLevel() == null) {
            String message = "pollutionLevel is required";
            log.warn("[AirPollutionSensorService][save] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }

        log.info("[AirPollutionSensorService][save] saving air pollution reading for location: {}", request.getLocation());

        AirPollutionSensorData entity = airPollutionSensorMapper.toEntity(request);
        AirPollutionSensorData savedEntity = airPollutionSensorRepository.save(entity);

        log.info("[AirPollutionSensorService][save] saved successfully with id: {}", savedEntity.getId());

        Map<Metric, Float> readings = new HashMap<>();
        readings.put(Metric.CO, savedEntity.getCo());
        readings.put(Metric.OZONE, savedEntity.getOzone());
        if (user.isPresent()) {
            alertService.checkAndTrigger(SensorType.AIR_POLLUTION, readings, savedEntity.getLocation(), user.get(), savedEntity.getId());
        } else {
            for (User u : userRepository.findAll()) {
                alertService.checkAndTrigger(SensorType.AIR_POLLUTION, readings, savedEntity.getLocation(), u, savedEntity.getId());
            }
        }

        return airPollutionSensorMapper.toResponse(savedEntity);
    }

    public List<AirPollutionSensorResponse> getAll() {
        log.info("[AirPollutionSensorService][getAll] fetch started: scope=all");

        List<AirPollutionSensorData> entities = airPollutionSensorRepository.findAllByOrderByTimestampDesc();
        List<AirPollutionSensorResponse> responses = entities.stream()
                .map(airPollutionSensorMapper::toResponse)
                .toList();

        log.info("[AirPollutionSensorService][getAll] fetch completed: count={}", responses.size());

        return responses;
    }

    public AirPollutionSensorResponse getById(String id) {
        log.info("[AirPollutionSensorService][getById] Fetching record with id: {}", id);
        UUID uuid = UUID.fromString(id);
        AirPollutionSensorData entity = airPollutionSensorRepository.findById(uuid)
                .orElseThrow(() -> {
                    log.warn("[AirPollutionSensorService][getById] Not found: {}", id);
                    return new ResourceNotFoundException("Air pollution sensor reading not found with id: " + id);
                });
        return airPollutionSensorMapper.toResponse(entity);
    }

    public AirPollutionSensorResponse getLatest() {
        AirPollutionSensorData entity = airPollutionSensorRepository.findTopByOrderByTimestampDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No air pollution sensor readings found"));
        log.info("[AirPollutionSensorService][getLatest] Returning latest record");
        return airPollutionSensorMapper.toResponse(entity);
    }

    public void flush() {
        log.info("[AirPollutionSensorService][flush] flush started: scope=all");

        airPollutionSensorRepository.deleteAll();

        log.info("[AirPollutionSensorService][flush] flush completed: ok");
    }
}
