package com.dxc.iotmonitor.alert.service;

import com.dxc.iotmonitor.alert.dto.response.AlertResponse;
import com.dxc.iotmonitor.alert.mapper.AlertMapper;
import com.dxc.iotmonitor.alert.model.AlertData;
import com.dxc.iotmonitor.alert.repository.AlertRepository;
import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.settings.model.ThresholdSetting;
import com.dxc.iotmonitor.settings.repository.ThresholdSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;
    private final ThresholdSettingRepository thresholdSettingRepository;

    public List<AlertResponse> findAll() {
        return alertRepository.findAllByOrderByTriggeredAtDesc().stream()
                .map(alertMapper::toResponse)
                .toList();
    }

    public AlertResponse findById(UUID id) {
        AlertData entity = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found."));
        return alertMapper.toResponse(entity);
    }

    public long count() {
        return alertRepository.count();
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

    public void checkAndTrigger(SensorType sensorType, String location, Map<Metric, Float> readings) {
        log.info("checkAndTrigger called — type={} location={}", sensorType, location);
        List<ThresholdSetting> settings = thresholdSettingRepository.findByType(sensorType);
        for (ThresholdSetting setting : settings) {
            Float value = readings.get(setting.getMetric());
            if (value == null) {
                continue;
            }
            boolean breach = false;
            if (setting.getAlertType() == AlertType.ABOVE && value > setting.getThresholdValue()) {
                breach = true;
            }
            if (setting.getAlertType() == AlertType.BELOW && value < setting.getThresholdValue()) {
                breach = true;
            }
            if (breach) {
                AlertData alert = AlertData.builder()
                        .sensorType(sensorType)
                        .location(location)
                        .metric(setting.getMetric())
                        .triggeredValue(value)
                        .thresholdValue(setting.getThresholdValue())
                        .alertType(setting.getAlertType())
                        .build();
                alertRepository.save(alert);
                log.info(
                        "ALERT TRIGGERED — type={} location={} metric={} value={} threshold={} alertType={}",
                        sensorType,
                        location,
                        setting.getMetric(),
                        value,
                        setting.getThresholdValue(),
                        setting.getAlertType());
            }
        }
    }
}
