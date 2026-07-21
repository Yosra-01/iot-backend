package com.dxc.iotmonitor.sensor.traffic.service;

import com.dxc.iotmonitor.alert.repository.AlertRepository;
import com.dxc.iotmonitor.enums.CongestionLevel;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.sensor.common.AlertFanOut;
import com.dxc.iotmonitor.sensor.common.SensorHandler;
import com.dxc.iotmonitor.sensor.common.SpecBuilder;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficFilterParams;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorRequest;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorResponse;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficStatsResponse;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficSensorHandler implements SensorHandler<TrafficSensorRequest, TrafficSensorResponse, TrafficFilterParams> {

    private final TrafficSensorRepository trafficSensorRepository;
    private final TrafficSensorMapper trafficSensorMapper;
    private final TrafficValidator trafficValidator;
    private final TrafficReadingsExtractor trafficReadingsExtractor;
    private final SpecBuilder<TrafficSensorData, TrafficFilterParams> trafficSpecBuilder;
    private final AlertFanOut alertFanOut;
    private final AlertRepository alertRepository;

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
        log.info("[TrafficSensorHandler][getFiltered] Fetching filtered & paginated traffic data");

        Specification<TrafficSensorData> spec = trafficSpecBuilder.build(filters);
        Page<TrafficSensorData> entities = trafficSensorRepository.findAll(spec, pageable);

        return entities.map(trafficSensorMapper::toResponse);
    }

    public TrafficStatsResponse getStats(LocalDateTime from, LocalDateTime to, String location) {
        log.info("[TrafficSensorHandler][getStats] Fetching stats: from={} to={} location={}", from, to, location);

        if (location != null && location.length() > 100) {
            throw new IllegalArgumentException("location must not exceed 100 characters");
        }
        if (from != null) {
            LocalDateTime effectiveTo = (to != null) ? to : LocalDateTime.now();
            if (from.isAfter(effectiveTo)) {
                throw new IllegalArgumentException("invalid date range: 'from' must be before 'to'");
            }
            if (ChronoUnit.DAYS.between(from, effectiveTo) > 90) {
                throw new IllegalArgumentException("range too wide for daily breakdown");
            }
        }

        TrafficSensorRepository.StatsProjection stats = trafficSensorRepository.findStats(from, to, location);
        List<TrafficSensorRepository.CongestionDistributionProjection> distributionList =
                trafficSensorRepository.findCongestionLevelDistribution(from, to, location);

        Map<CongestionLevel, Long> congestionLevelDistribution = distributionList.stream()
                .collect(Collectors.toMap(
                        TrafficSensorRepository.CongestionDistributionProjection::getCongestionLevel,
                        TrafficSensorRepository.CongestionDistributionProjection::getCount));

        List<TrafficStatsResponse.DailyAverage> dailyAverages = List.of();
        if (from != null && to != null) {
            List<TrafficSensorRepository.DailyAverageProjection> dailyList =
                    trafficSensorRepository.findDailyAverages(from, to, location);
            dailyAverages = dailyList.stream()
                    .map(p -> new TrafficStatsResponse.DailyAverage(
                            p.getDate().toString(),
                            p.getAvgTrafficDensity(),
                            p.getAvgSpeed()))
                    .toList();
        }

        long alertsTriggered = alertRepository.countAlerts(SensorType.TRAFFIC, location, from, to);

        return TrafficStatsResponse.builder()
                .from(from)
                .to(to)
                .location(location)
                .totalReadings(stats.getTotalReadings())
                .avgTrafficDensity(stats.getAvgTrafficDensity())
                .minTrafficDensity(stats.getMinTrafficDensity())
                .maxTrafficDensity(stats.getMaxTrafficDensity())
                .avgSpeed(stats.getAvgSpeed())
                .minSpeed(stats.getMinSpeed())
                .maxSpeed(stats.getMaxSpeed())
                .alertsTriggered(alertsTriggered)
                .congestionLevelDistribution(congestionLevelDistribution)
                .dailyAverages(dailyAverages)
                .build();
    }
}
