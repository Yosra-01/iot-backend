package com.dxc.iotmonitor.settings.service;

import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.settings.dto.ThresholdSettingRequest;
import com.dxc.iotmonitor.settings.dto.ThresholdSettingResponse;
import com.dxc.iotmonitor.settings.mapper.ThresholdSettingMapper;
import com.dxc.iotmonitor.settings.model.ThresholdSetting;
import com.dxc.iotmonitor.settings.repository.ThresholdSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThresholdSettingService {

    private final ThresholdSettingRepository thresholdSettingRepository;
    private final ThresholdSettingMapper thresholdSettingMapper;

    public List<ThresholdSettingResponse> upsert(List<ThresholdSettingRequest> requests) {
        // Step 1 — validate all requests first (no saves yet)
        for (ThresholdSettingRequest request : requests) {
            validateMetricForSensorType(request.getType(), request.getMetric());
            validateThresholdRange(request.getMetric(), request.getThresholdValue());
        }

        // Step 2 — contradiction check from incoming map + cross-check DB (no saves yet)
        Map<TypeMetricKey, Map<AlertType, Float>> incomingByKey = new HashMap<>();
        for (ThresholdSettingRequest request : requests) {
            TypeMetricKey key = new TypeMetricKey(request.getType(), request.getMetric());
            incomingByKey
                    .computeIfAbsent(key, k -> new EnumMap<>(AlertType.class))
                    .put(request.getAlertType(), request.getThresholdValue());
        }

        for (Map.Entry<TypeMetricKey, Map<AlertType, Float>> entry : incomingByKey.entrySet()) {
            TypeMetricKey key = entry.getKey();
            Map<AlertType, Float> incoming = entry.getValue();
            Float aboveIncoming = incoming.get(AlertType.ABOVE);
            Float belowIncoming = incoming.get(AlertType.BELOW);

            if (aboveIncoming != null && belowIncoming != null && !(belowIncoming < aboveIncoming)) {
                throwContradiction(key.metric());
            }

            if (aboveIncoming != null) {
                Optional<ThresholdSetting> dbBelow = thresholdSettingRepository.findByTypeAndMetricAndAlertType(
                        key.type(), key.metric(), AlertType.BELOW);
                if (dbBelow.isPresent() && !(dbBelow.get().getThresholdValue() < aboveIncoming)) {
                    throwContradiction(key.metric());
                }
            }

            if (belowIncoming != null) {
                Optional<ThresholdSetting> dbAbove = thresholdSettingRepository.findByTypeAndMetricAndAlertType(
                        key.type(), key.metric(), AlertType.ABOVE);
                if (dbAbove.isPresent() && !(belowIncoming < dbAbove.get().getThresholdValue())) {
                    throwContradiction(key.metric());
                }
            }
        }

        // Step 3 — save all (only after validations pass)
        for (ThresholdSettingRequest request : requests) {
            Optional<ThresholdSetting> existing = thresholdSettingRepository.findByTypeAndMetricAndAlertType(
                    request.getType(), request.getMetric(), request.getAlertType());

            ThresholdSetting entity;
            if (existing.isPresent()) {
                entity = existing.get();
                entity.setThresholdValue(request.getThresholdValue());
            } else {
                entity = thresholdSettingMapper.toEntity(request);
            }

            log.info("[ThresholdSettingService][upsert] saving {} threshold for metric: {}", request.getAlertType(), request.getMetric());
            ThresholdSetting saved = thresholdSettingRepository.save(entity);
            log.info("[ThresholdSettingService][upsert] saved successfully with id: {}", saved.getId());
        }

        // Step 4 — return full list mapped to responses
        return thresholdSettingRepository.findAll().stream()
                .map(thresholdSettingMapper::toResponse)
                .toList();
    }

    private void throwContradiction(Metric metric) {
        String message = "Contradictory thresholds: below value must be less than above value for metric " + metric.name() + ".";
        log.warn("[ThresholdSettingService][upsert] validation failed: {}", message);
        throw new IllegalArgumentException(message);
    }

    public List<ThresholdSettingResponse> findAll() {
        List<ThresholdSetting> all = thresholdSettingRepository.findAll();
        log.info("[ThresholdSettingService][findAll] returning {} settings", all.size());
        return all.stream()
                .map(thresholdSettingMapper::toResponse)
                .toList();
    }

    public void flush() {
        thresholdSettingRepository.deleteAll();
        log.info("[ThresholdSettingService][flush] all settings deleted");
    }

    private void validateMetricForSensorType(SensorType type, Metric metric) {
        boolean valid = switch (type) {
            case TRAFFIC -> metric == Metric.TRAFFIC_DENSITY || metric == Metric.AVG_SPEED;
            case AIR_POLLUTION -> metric == Metric.CO || metric == Metric.OZONE;
            case STREET_LIGHT -> metric == Metric.BRIGHTNESS_LEVEL || metric == Metric.POWER_CONSUMPTION;
        };
        if (!valid) {
            String message = "invalid metric for this sensor type";
            log.warn("[ThresholdSettingService][upsert] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
    }

    private void validateThresholdRange(Metric metric, Float thresholdValue) {
        if (thresholdValue == null) {
            String message = "thresholdValue out of valid range for this metric";
            log.warn("[ThresholdSettingService][upsert] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
        float v = thresholdValue;
        boolean inRange = switch (metric) {
            case TRAFFIC_DENSITY -> v >= 0f && v <= 500f;
            case AVG_SPEED -> v >= 0f && v <= 120f;
            case CO -> v >= 0f && v <= 50f;
            case OZONE -> v >= 0f && v <= 300f;
            case BRIGHTNESS_LEVEL -> v >= 0f && v <= 100f;
            case POWER_CONSUMPTION -> v >= 0f && v <= 5000f;
        };
        if (!inRange) {
            String message = "thresholdValue out of valid range for this metric";
            log.warn("[ThresholdSettingService][upsert] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
    }

    private record TypeMetricKey(SensorType type, Metric metric) {}
}
