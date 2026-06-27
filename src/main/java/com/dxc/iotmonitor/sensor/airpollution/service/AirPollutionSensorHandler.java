package com.dxc.iotmonitor.sensor.airpollution.service;

import com.dxc.iotmonitor.alert.repository.AlertRepository;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.PollutionLevel;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionFilterParams;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorRequest;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorResponse;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionStatsResponse;
import com.dxc.iotmonitor.sensor.airpollution.mapper.AirPollutionSensorMapper;
import com.dxc.iotmonitor.sensor.airpollution.model.AirPollutionSensorData;
import com.dxc.iotmonitor.sensor.airpollution.repository.AirPollutionSensorRepository;
import com.dxc.iotmonitor.sensor.common.AlertFanOut;
import com.dxc.iotmonitor.sensor.common.SensorHandler;
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
public class AirPollutionSensorHandler implements SensorHandler<AirPollutionSensorData, AirPollutionSensorRequest, AirPollutionSensorResponse, AirPollutionFilterParams> {

    private final AirPollutionSensorRepository airPollutionSensorRepository;
    private final AirPollutionSensorMapper airPollutionSensorMapper;
    private final AirPollutionValidator airPollutionValidator;
    private final AirPollutionReadingsExtractor airPollutionReadingsExtractor;
    private final AirPollutionSpecBuilder airPollutionSpecBuilder;
    private final AlertFanOut alertFanOut;
    private final AlertRepository alertRepository;

    @Override
    public AirPollutionSensorResponse save(AirPollutionSensorRequest request, Optional<User> user) {
        airPollutionValidator.validate(request);

        log.info("[AirPollutionSensorHandler][save] saving air pollution reading for location: {}", request.getLocation());

        AirPollutionSensorData entity = airPollutionSensorMapper.toEntity(request);
        AirPollutionSensorData savedEntity = airPollutionSensorRepository.save(entity);

        log.info("[AirPollutionSensorHandler][save] saved successfully with id: {}", savedEntity.getId());

        Map<Metric, Float> readings = airPollutionReadingsExtractor.extract(savedEntity);
        alertFanOut.fanOut(SensorType.AIR_POLLUTION, readings, savedEntity.getLocation(), user, savedEntity.getId());

        return airPollutionSensorMapper.toResponse(savedEntity);
    }

    @Override
    public AirPollutionSensorResponse getById(String id) {
        log.info("[AirPollutionSensorHandler][getById] Fetching record with id: {}", id);
        UUID uuid = UUID.fromString(id);
        AirPollutionSensorData entity = airPollutionSensorRepository.findById(uuid)
                .orElseThrow(() -> {
                    log.warn("[AirPollutionSensorHandler][getById] Not found: {}", id);
                    return new ResourceNotFoundException("Air pollution sensor reading not found with id: " + id);
                });
        return airPollutionSensorMapper.toResponse(entity);
    }

    @Override
    public AirPollutionSensorResponse getLatest() {
        AirPollutionSensorData entity = airPollutionSensorRepository.findTopByOrderByTimestampDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No air pollution sensor readings found"));
        log.info("[AirPollutionSensorHandler][getLatest] Returning latest record");
        return airPollutionSensorMapper.toResponse(entity);
    }

    @Override
    public void flush() {
        log.info("[AirPollutionSensorHandler][flush] flush started: scope=all");
        airPollutionSensorRepository.deleteAll();
        log.info("[AirPollutionSensorHandler][flush] flush completed: ok");
    }

    @Override
    public Page<AirPollutionSensorResponse> getFiltered(AirPollutionFilterParams filters, Pageable pageable) {
        log.info("[AirPollutionSensorHandler][getFiltered] Fetching filtered & paginated air pollution data");

        Specification<AirPollutionSensorData> spec = airPollutionSpecBuilder.build(filters);
        Page<AirPollutionSensorData> entities = airPollutionSensorRepository.findAll(spec, pageable);

        return entities.map(airPollutionSensorMapper::toResponse);
    }

    public AirPollutionStatsResponse getStats(LocalDateTime from, LocalDateTime to, String location) {
        log.info("[AirPollutionSensorHandler][getStats] Fetching stats: from={} to={} location={}", from, to, location);

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

        AirPollutionSensorRepository.StatsProjection stats = airPollutionSensorRepository.findStats(from, to, location);
        List<AirPollutionSensorRepository.PollutionDistributionProjection> distributionList =
                airPollutionSensorRepository.findPollutionLevelDistribution(from, to, location);

        Map<PollutionLevel, Long> pollutionLevelDistribution = distributionList.stream()
                .collect(Collectors.toMap(
                        AirPollutionSensorRepository.PollutionDistributionProjection::getPollutionLevel,
                        AirPollutionSensorRepository.PollutionDistributionProjection::getCount));

        List<AirPollutionStatsResponse.DailyAverage> dailyAverages = List.of();
        if (from != null && to != null) {
            List<AirPollutionSensorRepository.DailyAverageProjection> dailyList =
                    airPollutionSensorRepository.findDailyAverages(from, to, location);
            dailyAverages = dailyList.stream()
                    .map(p -> new AirPollutionStatsResponse.DailyAverage(
                            p.getDate().toString(),
                            p.getAvgCo(),
                            p.getAvgOzone()))
                    .toList();
        }

        long alertsTriggered = alertRepository.countAlerts(SensorType.AIR_POLLUTION, location, from, to);

        return AirPollutionStatsResponse.builder()
                .from(from)
                .to(to)
                .location(location)
                .totalReadings(stats.getTotalReadings())
                .avgCo(stats.getAvgCo())
                .minCo(stats.getMinCo())
                .maxCo(stats.getMaxCo())
                .avgOzone(stats.getAvgOzone())
                .minOzone(stats.getMinOzone())
                .maxOzone(stats.getMaxOzone())
                .alertsTriggered(alertsTriggered)
                .pollutionLevelDistribution(pollutionLevelDistribution)
                .dailyAverages(dailyAverages)
                .build();
    }
}
