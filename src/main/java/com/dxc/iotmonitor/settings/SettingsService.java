package com.dxc.iotmonitor.settings;

import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.settings.dto.SettingsRequest;
import com.dxc.iotmonitor.settings.dto.SettingsResponse;
import com.dxc.iotmonitor.user.model.User;
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
public class SettingsService {

    private final SettingsRepository settingsRepository;
    private final SettingsMapper settingsMapper;

    public List<SettingsResponse> upsert(List<SettingsRequest> requests, User user) {
        for (SettingsRequest request : requests) {
            validateMetricForSensorType(request.getType(), request.getMetric());
            validateThresholdRange(request.getMetric(), request.getThresholdValue());
        }

        Map<TypeMetricKey, Map<AlertType, Float>> incomingByKey = new HashMap<>();
        for (SettingsRequest request : requests) {
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
                Optional<Settings> dbBelow = settingsRepository.findByUserAndTypeAndMetricAndAlertType(
                        user, key.type(), key.metric(), AlertType.BELOW);
                if (dbBelow.isPresent() && !(dbBelow.get().getThresholdValue() < aboveIncoming)) {
                    throwContradiction(key.metric());
                }
            }

            if (belowIncoming != null) {
                Optional<Settings> dbAbove = settingsRepository.findByUserAndTypeAndMetricAndAlertType(
                        user, key.type(), key.metric(), AlertType.ABOVE);
                if (dbAbove.isPresent() && !(belowIncoming < dbAbove.get().getThresholdValue())) {
                    throwContradiction(key.metric());
                }
            }
        }

        for (SettingsRequest request : requests) {
            Optional<Settings> existing = settingsRepository.findByUserAndTypeAndMetricAndAlertType(
                    user, request.getType(), request.getMetric(), request.getAlertType());

            Settings entity;
            if (existing.isPresent()) {
                entity = existing.get();
                entity.setThresholdValue(request.getThresholdValue());
            } else {
                entity = settingsMapper.toEntity(request);
                entity.setUser(user);
            }

            log.info("[SettingsService][upsert] saving {} threshold for metric: {}", request.getAlertType(), request.getMetric());
            Settings saved = settingsRepository.save(entity);
            log.info("[SettingsService][upsert] saved successfully with id: {}", saved.getId());
        }

        return settingsRepository.findByUser(user).stream()
                .map(settingsMapper::toResponse)
                .toList();
    }

    private void throwContradiction(Metric metric) {
        String message = "Contradictory thresholds: below value must be less than above value for metric " + metric.name() + ".";
        log.warn("[SettingsService][upsert] validation failed: {}", message);
        throw new IllegalArgumentException(message);
    }

    public List<SettingsResponse> findAll(User user) {
        List<Settings> all = settingsRepository.findByUser(user);
        log.info("[SettingsService][findAll] returning {} settings", all.size());
        return all.stream()
                .map(settingsMapper::toResponse)
                .toList();
    }

    public void flush() {
        settingsRepository.deleteAll();
        log.info("[SettingsService][flush] all settings deleted");
    }

    private void validateMetricForSensorType(SensorType type, Metric metric) {
        boolean valid = switch (type) {
            case TRAFFIC -> metric == Metric.TRAFFIC_DENSITY || metric == Metric.AVG_SPEED;
            case AIR_POLLUTION -> metric == Metric.CO || metric == Metric.OZONE;
            case STREET_LIGHT -> metric == Metric.BRIGHTNESS_LEVEL || metric == Metric.POWER_CONSUMPTION;
        };
        if (!valid) {
            String message = "invalid metric for this sensor type";
            log.warn("[SettingsService][upsert] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
    }

    private void validateThresholdRange(Metric metric, Float thresholdValue) {
        if (thresholdValue == null) {
            String message = "thresholdValue out of valid range for this metric";
            log.warn("[SettingsService][upsert] validation failed: {}", message);
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
            log.warn("[SettingsService][upsert] validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
    }

    private record TypeMetricKey(SensorType type, Metric metric) {}
}
