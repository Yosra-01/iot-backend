package com.dxc.iotmonitor.settings;

import com.dxc.iotmonitor.settings.dto.SettingsRequest;
import com.dxc.iotmonitor.settings.dto.SettingsResponse;
import org.springframework.stereotype.Component;

@Component
public class SettingsMapper {

    public Settings toEntity(SettingsRequest request) {
        Settings entity = new Settings();
        entity.setType(request.getType());
        entity.setMetric(request.getMetric());
        entity.setThresholdValue(request.getThresholdValue());
        entity.setAlertType(request.getAlertType());
        return entity;
    }

    public SettingsResponse toResponse(Settings entity) {
        SettingsResponse response = new SettingsResponse();
        response.setId(entity.getId());
        response.setType(entity.getType());
        response.setMetric(entity.getMetric());
        response.setThresholdValue(entity.getThresholdValue());
        response.setAlertType(entity.getAlertType());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}
