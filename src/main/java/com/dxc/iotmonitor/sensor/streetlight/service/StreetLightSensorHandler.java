package com.dxc.iotmonitor.sensor.streetlight.service;

import com.dxc.iotmonitor.alert.repository.AlertRepository;
import com.dxc.iotmonitor.enums.LightStatus;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.sensor.common.AlertFanOut;
import com.dxc.iotmonitor.sensor.common.SensorHandler;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightFilterParams;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorRequest;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorResponse;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightStatsResponse;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final AlertRepository alertRepository;

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
        log.info("[StreetLightSensorHandler][getFiltered] Fetching filtered & paginated street light data");

        Specification<StreetLightSensorData> spec = streetLightSpecBuilder.build(filters);
        Page<StreetLightSensorData> entities = streetLightSensorRepository.findAll(spec, pageable);

        return entities.map(streetLightSensorMapper::toResponse);
    }

    public StreetLightStatsResponse getStats(LocalDateTime from, LocalDateTime to, String location) {
        log.info("[StreetLightSensorHandler][getStats] Fetching stats: from={} to={} location={}", from, to, location);

        if (location != null && location.length() > 100) {
            throw new IllegalArgumentException("location must not exceed 100 characters");
        }
        if (from != null) {
            LocalDateTime effectiveTo = (to != null) ? to : LocalDateTime.now();
            if (from.isAfter(effectiveTo)) {
                throw new IllegalArgumentException("invalid date range: 'from' must be before 'to'");
            }
            if (Duration.between(from, effectiveTo).toDays() > 90) {
                throw new IllegalArgumentException("range too wide for daily breakdown");
            }
        }

        StreetLightSensorRepository.StatsProjection stats = streetLightSensorRepository.findStats(from, to, location);
        List<StreetLightSensorRepository.StatusDistributionProjection> distributionList =
                streetLightSensorRepository.findStatusDistribution(from, to, location);

        Map<LightStatus, Long> statusDistribution = distributionList.stream()
                .collect(Collectors.toMap(
                        StreetLightSensorRepository.StatusDistributionProjection::getStatus,
                        StreetLightSensorRepository.StatusDistributionProjection::getCount));

        List<StreetLightStatsResponse.DailyAverage> dailyAverages = List.of();
        if (from != null && to != null) {
            List<StreetLightSensorRepository.DailyAverageProjection> dailyList =
                    streetLightSensorRepository.findDailyAverages(from, to, location);
            dailyAverages = dailyList.stream()
                    .map(p -> new StreetLightStatsResponse.DailyAverage(
                            p.getDate().toString(),
                            p.getAvgBrightness(),
                            p.getAvgPowerConsumption()))
                    .toList();
        }

        long alertsTriggered = alertRepository.countAlerts(SensorType.STREET_LIGHT, location, from, to);

        return StreetLightStatsResponse.builder()
                .from(from)
                .to(to)
                .location(location)
                .totalReadings(stats.getTotalReadings())
                .avgBrightness(stats.getAvgBrightness())
                .minBrightness(stats.getMinBrightness())
                .maxBrightness(stats.getMaxBrightness())
                .avgPowerConsumption(stats.getAvgPowerConsumption())
                .minPowerConsumption(stats.getMinPowerConsumption())
                .maxPowerConsumption(stats.getMaxPowerConsumption())
                .alertsTriggered(alertsTriggered)
                .statusDistribution(statusDistribution)
                .dailyAverages(dailyAverages)
                .build();
    }
}
