package com.dxc.iotmonitor.sensor.traffic.service;

import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.sensor.common.AlertFanOut;
import com.dxc.iotmonitor.sensor.common.SensorHandler;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficFilterParams;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorRequest;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorResponse;
import com.dxc.iotmonitor.sensor.traffic.mapper.TrafficSensorMapper;
import com.dxc.iotmonitor.sensor.traffic.model.TrafficSensorData;
import com.dxc.iotmonitor.sensor.traffic.repository.TrafficSensorRepository;
import com.dxc.iotmonitor.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficSensorHandler implements SensorHandler<TrafficSensorData, TrafficSensorRequest, TrafficSensorResponse, TrafficFilterParams> {

    private final TrafficSensorRepository trafficSensorRepository;
    private final TrafficSensorMapper trafficSensorMapper;
    private final TrafficValidator trafficValidator;
    private final TrafficReadingsExtractor trafficReadingsExtractor;
    private final TrafficSpecBuilder trafficSpecBuilder;
    private final AlertFanOut alertFanOut;

    @Override
    public TrafficSensorResponse save(TrafficSensorRequest request, Optional<User> user) {
        trafficValidator.validate(request);

        log.info("[TrafficSensorHandler][save] saving traffic reading for location: {}", request.getLocation());

        TrafficSensorData entity = trafficSensorMapper.toEntity(request);
        TrafficSensorData savedEntity = trafficSensorRepository.save(entity);

        log.info("[TrafficSensorHandler][save] saved successfully with id: {}", savedEntity.getId());

        Map<Metric, Float> readings = trafficReadingsExtractor.extract(savedEntity);
        alertFanOut.fanOut(SensorType.TRAFFIC, readings, savedEntity.getLocation(), user, savedEntity.getId());

        return trafficSensorMapper.toResponse(savedEntity);
    }

    @Override
    public TrafficSensorResponse getById(String id) {
        log.info("[TrafficSensorHandler][getById] Fetching record with id: {}", id);
        UUID uuid = UUID.fromString(id);
        TrafficSensorData entity = trafficSensorRepository.findById(uuid)
                .orElseThrow(() -> {
                    log.warn("[TrafficSensorHandler][getById] Not found: {}", id);
                    return new ResourceNotFoundException("Traffic sensor reading not found with id: " + id);
                });
        return trafficSensorMapper.toResponse(entity);
    }

    @Override
    public TrafficSensorResponse getLatest() {
        TrafficSensorData entity = trafficSensorRepository.findTopByOrderByTimestampDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No traffic sensor readings found"));
        log.info("[TrafficSensorHandler][getLatest] Returning latest record");
        return trafficSensorMapper.toResponse(entity);
    }

    @Override
    public void flush() {
        log.info("[TrafficSensorHandler][flush] flush started: scope=all");
        trafficSensorRepository.deleteAll();
        log.info("[TrafficSensorHandler][flush] flush completed: ok");
    }

    @Override
    public Page<TrafficSensorResponse> getFiltered(TrafficFilterParams filters, Pageable pageable) {
        log.info("[TrafficSensorHandler] Fetching filtered & paginated traffic data");

        Specification<TrafficSensorData> spec = trafficSpecBuilder.build(filters);
        Page<TrafficSensorData> entities = trafficSensorRepository.findAll(spec, pageable);

        return entities.map(trafficSensorMapper::toResponse);
    }
}
