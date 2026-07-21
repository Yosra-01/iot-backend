package com.dxc.iotmonitor.alert.service;

import com.dxc.iotmonitor.alert.AlertData;
import com.dxc.iotmonitor.alert.dto.AlertFilterParams;
import com.dxc.iotmonitor.alert.dto.response.AlertResponse;
import com.dxc.iotmonitor.alert.mapper.AlertMapper;
import com.dxc.iotmonitor.alert.repository.AlertRepository;
import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.settings.model.Settings;
import com.dxc.iotmonitor.settings.repository.SettingsRepository;
import com.dxc.iotmonitor.sensor.common.SpecBuilder;
import com.dxc.iotmonitor.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private static final String ALERT_NOT_FOUND = "Alert not found.";

    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;
    private final SettingsRepository settingsRepository;
    private final SpecBuilder<AlertData, AlertFilterParams> alertSpecBuilder;

    public Page<AlertResponse> findFiltered(AlertFilterParams filters, Pageable pageable, User user) {
        Specification<AlertData> userSpec = (root, query, cb) -> cb.equal(root.get("user"), user);
        Specification<AlertData> filterSpec = alertSpecBuilder.build(filters);
        Page<AlertData> entities = alertRepository.findAll(userSpec.and(filterSpec), pageable);
        return entities.map(alertMapper::toResponse);
    }

    public AlertResponse findById(UUID id, User user) {
        AlertData entity = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ALERT_NOT_FOUND));
        assertOwnedByUser(entity, user, id);
        return alertMapper.toResponse(entity);
    }

    public long count(AlertFilterParams filters, User user) {
        Specification<AlertData> userSpec = (root, query, cb) -> cb.equal(root.get("user"), user);
        Specification<AlertData> filterSpec = alertSpecBuilder.build(filters);
        return alertRepository.count(userSpec.and(filterSpec));
    }

    public void deleteById(UUID id, User user) {
        AlertData entity = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ALERT_NOT_FOUND));
        assertOwnedByUser(entity, user, id);
        alertRepository.deleteById(id);
        log.info("[AlertService][deleteById] Alert dismissed: id={} by user={}", id, user.getUserId());
    }

    private void assertOwnedByUser(AlertData alert, User user, UUID id) {
        if (!alert.getUser().getUserId().equals(user.getUserId())) {
            log.warn("[AlertService][assertOwnedByUser] Alert access denied: user {} does not own alert {}", user.getUserId(), id);
            throw new AccessDeniedException("You do not have permission to access this alert.");
        }
    }

    public void flush() {
        alertRepository.deleteAll();
        log.info("[AlertService][flush] All alerts flushed.");
    }

    @Transactional
    public void markAsRead(UUID id, User user) {
        AlertData alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ALERT_NOT_FOUND));
        assertOwnedByUser(alert, user, id);
        if (alert.getReadAt() == null) {
            alert.setReadAt(LocalDateTime.now(ZoneId.of("Africa/Cairo")));
            alertRepository.save(alert);
            log.info("[AlertService][markAsRead] Alert marked as read: id={} by user={}", id, user.getUserId());
        }
    }

    @Transactional
    public void checkAndTrigger(SensorType type, Map<Metric, Float> values, String location, User user, UUID readingId) {
        log.info("[AlertService][checkAndTrigger] checkAndTrigger called — type={} location={} user={}", type, location, user.getUserId());
        List<Settings> settings = settingsRepository.findByUser(user);
        List<AlertData> alerts = new ArrayList<>();
        for (Settings setting : settings) {
            isBreached(setting, type, values).ifPresent(actualValue ->
                    alerts.add(buildAlert(setting, type, location, user, readingId, actualValue)));
        }
        if (!alerts.isEmpty()) {
            alertRepository.saveAll(alerts);
        }
    }

    private Optional<Float> isBreached(Settings setting, SensorType type, Map<Metric, Float> values) {
        if (setting.getType() != type) {
            return Optional.empty();
        }
        if (!values.containsKey(setting.getMetric())) {
            return Optional.empty();
        }
        Float actualValue = values.get(setting.getMetric());
        if (actualValue == null) {
            return Optional.empty();
        }
        boolean breach = false;
        if (setting.getAlertType() == AlertType.ABOVE && actualValue > setting.getThresholdValue()) {
            breach = true;
        }
        if (setting.getAlertType() == AlertType.BELOW && actualValue < setting.getThresholdValue()) {
            breach = true;
        }
        return breach ? Optional.of(actualValue) : Optional.empty();
    }

    private AlertData buildAlert(Settings setting, SensorType type, String location, User user,
                                 UUID readingId, Float actualValue) {
        AlertData alert = AlertData.builder()
                .user(user)
                .sensorType(type)
                .location(location)
                .metric(setting.getMetric())
                .triggeredValue(actualValue)
                .thresholdValue(setting.getThresholdValue())
                .alertType(setting.getAlertType())
                .readingId(readingId)
                .build();
        log.info(
                "[AlertService][checkAndTrigger] ALERT TRIGGERED — type={} location={} metric={} value={} threshold={} alertType={}",
                type,
                location,
                setting.getMetric(),
                actualValue,
                setting.getThresholdValue(),
                setting.getAlertType());
        return alert;
    }
}
