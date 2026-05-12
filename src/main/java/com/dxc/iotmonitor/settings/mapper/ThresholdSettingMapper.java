package com.dxc.iotmonitor.settings.mapper;

import com.dxc.iotmonitor.settings.dto.ThresholdSettingRequest;
import com.dxc.iotmonitor.settings.dto.ThresholdSettingResponse;
import com.dxc.iotmonitor.settings.model.ThresholdSetting;
import org.springframework.stereotype.Component;

@Component
public class ThresholdSettingMapper {

    public ThresholdSetting toEntity(ThresholdSettingRequest request) {
        ThresholdSetting entity = new ThresholdSetting();
        entity.setType(request.getType());
        entity.setMetric(request.getMetric());
        entity.setThresholdValue(request.getThresholdValue());
        entity.setAlertType(request.getAlertType());
        return entity;
    }

    public ThresholdSettingResponse toResponse(ThresholdSetting entity) {
        ThresholdSettingResponse response = new ThresholdSettingResponse();
        response.setId(entity.getId());
        response.setType(entity.getType());
        response.setMetric(entity.getMetric());
        response.setThresholdValue(entity.getThresholdValue());
        response.setAlertType(entity.getAlertType());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}
