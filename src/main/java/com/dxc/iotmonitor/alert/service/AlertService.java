package com.dxc.iotmonitor.alert.service;

import com.dxc.iotmonitor.alert.AlertData;
import com.dxc.iotmonitor.alert.dto.response.AlertResponse;
import com.dxc.iotmonitor.alert.mapper.AlertMapper;
import com.dxc.iotmonitor.alert.repository.AlertRepository;
import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.settings.model.Settings;
import com.dxc.iotmonitor.settings.repository.SettingsRepository;
import com.dxc.iotmonitor.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;
    private final SettingsRepository settingsRepository;

    public List<AlertResponse> findAll(User user) {
        return alertRepository.findByUserOrderByTriggeredAtDesc(user).stream()
                .map(alertMapper::toResponse)
                .toList();
    }

    public AlertResponse findById(UUID id) {
        AlertData entity = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found."));
        return alertMapper.toResponse(entity);
    }

    public long count(User user) {
        return alertRepository.countByUser(user);
    }

    public void deleteById(UUID id) {
        if (alertRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Alert not found.");
        }
        alertRepository.deleteById(id);
        log.info("Alert dismissed: {}", id);
    }

    public void flush() {
        alertRepository.deleteAll();
        log.info("All alerts flushed.");
    }

    public void checkAndTrigger(SensorType type, Map<Metric, Float> values, String location, User user, UUID readingId) {
        log.info("checkAndTrigger called — type={} location={} user={}", type, location, user.getUserId());
        List<Settings> settings = settingsRepository.findByUser(user);
        for (Settings setting : settings) {
            if (setting.getType() != type) {
                continue;
            }
            if (!values.containsKey(setting.getMetric())) {
                continue;
            }
            Float actualValue = values.get(setting.getMetric());
            if (actualValue == null) {
                continue;
            }
            boolean breach = false;
            if (setting.getAlertType() == AlertType.ABOVE && actualValue > setting.getThresholdValue()) {
                breach = true;
            }
            if (setting.getAlertType() == AlertType.BELOW && actualValue < setting.getThresholdValue()) {
                breach = true;
            }
            if (breach) {
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
                alertRepository.save(alert);
                log.info(
                        "ALERT TRIGGERED — type={} location={} metric={} value={} threshold={} alertType={}",
                        type,
                        location,
                        setting.getMetric(),
                        actualValue,
                        setting.getThresholdValue(),
                        setting.getAlertType());
            }
        }
    }
}
