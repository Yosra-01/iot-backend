package com.dxc.iotmonitor.sensor.streetlight.service;

import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.sensor.common.AlertFanOut;
import com.dxc.iotmonitor.sensor.common.SensorHandler;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightFilterParams;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorRequest;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorResponse;
import com.dxc.iotmonitor.sensor.streetlight.mapper.StreetLightSensorMapper;
import com.dxc.iotmonitor.sensor.streetlight.model.StreetLightSensorData;
import com.dxc.iotmonitor.sensor.streetlight.repository.StreetLightSensorRepository;
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
public class StreetLightSensorHandler implements SensorHandler<StreetLightSensorData, StreetLightSensorRequest, StreetLightSensorResponse, StreetLightFilterParams> {

    private final StreetLightSensorRepository streetLightSensorRepository;
    private final StreetLightSensorMapper streetLightSensorMapper;
    private final StreetLightValidator streetLightValidator;
    private final StreetLightReadingsExtractor streetLightReadingsExtractor;
    private final StreetLightSpecBuilder streetLightSpecBuilder;
    private final AlertFanOut alertFanOut;

    @Override
    public StreetLightSensorResponse save(StreetLightSensorRequest request, Optional<User> user) {
        streetLightValidator.validate(request);

        log.info("[StreetLightSensorHandler][save] saving street light reading for location: {}", request.getLocation());

        StreetLightSensorData entity = streetLightSensorMapper.toEntity(request);
        StreetLightSensorData savedEntity = streetLightSensorRepository.save(entity);

        log.info("[StreetLightSensorHandler][save] saved successfully with id: {}", savedEntity.getId());

        Map<Metric, Float> readings = streetLightReadingsExtractor.extract(savedEntity);
        alertFanOut.fanOut(SensorType.STREET_LIGHT, readings, savedEntity.getLocation(), user, savedEntity.getId());

        return streetLightSensorMapper.toResponse(savedEntity);
    }

    @Override
    public StreetLightSensorResponse getById(String id) {
        log.info("[StreetLightSensorHandler][getById] Fetching record with id: {}", id);
        UUID uuid = UUID.fromString(id);
        StreetLightSensorData entity = streetLightSensorRepository.findById(uuid)
                .orElseThrow(() -> {
                    log.warn("[StreetLightSensorHandler][getById] Not found: {}", id);
                    return new ResourceNotFoundException("Street light sensor reading not found with id: " + id);
                });
        return streetLightSensorMapper.toResponse(entity);
    }

    @Override
    public StreetLightSensorResponse getLatest() {
        StreetLightSensorData entity = streetLightSensorRepository.findTopByOrderByTimestampDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No street light sensor readings found"));
        log.info("[StreetLightSensorHandler][getLatest] Returning latest record");
        return streetLightSensorMapper.toResponse(entity);
    }

    @Override
    public void flush() {
        log.info("[StreetLightSensorHandler][flush] flush started: scope=all");
        streetLightSensorRepository.deleteAll();
        log.info("[StreetLightSensorHandler][flush] flush completed: ok");
    }

    @Override
    public Page<StreetLightSensorResponse> getFiltered(StreetLightFilterParams filters, Pageable pageable) {
        log.info("[StreetLightSensorHandler] Fetching filtered & paginated street light data");

        Specification<StreetLightSensorData> spec = streetLightSpecBuilder.build(filters);
        Page<StreetLightSensorData> entities = streetLightSensorRepository.findAll(spec, pageable);

        return entities.map(streetLightSensorMapper::toResponse);
    }
}
