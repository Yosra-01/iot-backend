package com.dxc.iotmonitor.settings;

import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.settings.dto.ThresholdSettingRequest;
import com.dxc.iotmonitor.settings.dto.ThresholdSettingResponse;
import com.dxc.iotmonitor.settings.mapper.ThresholdSettingMapper;
import com.dxc.iotmonitor.settings.model.ThresholdSetting;
import com.dxc.iotmonitor.settings.repository.ThresholdSettingRepository;
import com.dxc.iotmonitor.settings.service.ThresholdSettingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThresholdSettingServiceTest {

    @Mock
    private ThresholdSettingRepository thresholdSettingRepository;

    @Mock
    private ThresholdSettingMapper thresholdSettingMapper;

    @InjectMocks
    private ThresholdSettingService thresholdSettingService;

    @Test
    void upsert_savesNewRule_whenNotExists() {
        ThresholdSettingRequest request = new ThresholdSettingRequest();
        request.setType(SensorType.TRAFFIC);
        request.setMetric(Metric.TRAFFIC_DENSITY);
        request.setThresholdValue(400f);
        request.setAlertType(AlertType.ABOVE);

        when(thresholdSettingRepository.findByTypeAndMetricAndAlertType(
                SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.BELOW))
                .thenReturn(Optional.empty());
        when(thresholdSettingRepository.findByTypeAndMetricAndAlertType(
                SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE))
                .thenReturn(Optional.empty());

        ThresholdSetting mappedEntity = new ThresholdSetting();
        mappedEntity.setType(SensorType.TRAFFIC);
        mappedEntity.setMetric(Metric.TRAFFIC_DENSITY);
        mappedEntity.setThresholdValue(400f);
        mappedEntity.setAlertType(AlertType.ABOVE);

        when(thresholdSettingMapper.toEntity(request)).thenReturn(mappedEntity);

        UUID savedId = UUID.randomUUID();
        ThresholdSetting savedEntity = new ThresholdSetting();
        savedEntity.setId(savedId);
        savedEntity.setType(SensorType.TRAFFIC);
        savedEntity.setMetric(Metric.TRAFFIC_DENSITY);
        savedEntity.setThresholdValue(400f);
        savedEntity.setAlertType(AlertType.ABOVE);
        savedEntity.setCreatedAt(LocalDateTime.now());

        when(thresholdSettingRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(thresholdSettingRepository.findAll()).thenReturn(List.of(savedEntity));

        ThresholdSettingResponse response = new ThresholdSettingResponse();
        response.setId(savedId);
        response.setType(SensorType.TRAFFIC);
        response.setMetric(Metric.TRAFFIC_DENSITY);
        response.setThresholdValue(400f);
        response.setAlertType(AlertType.ABOVE);
        response.setCreatedAt(savedEntity.getCreatedAt());

        when(thresholdSettingMapper.toResponse(savedEntity)).thenReturn(response);

        List<ThresholdSettingResponse> result = thresholdSettingService.upsert(List.of(request));

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(thresholdSettingRepository).save(any(ThresholdSetting.class));
    }

    @Test
    void upsert_updatesExistingRule_whenExists() {
        ThresholdSettingRequest request = new ThresholdSettingRequest();
        request.setType(SensorType.TRAFFIC);
        request.setMetric(Metric.TRAFFIC_DENSITY);
        request.setThresholdValue(400f);
        request.setAlertType(AlertType.ABOVE);

        when(thresholdSettingRepository.findByTypeAndMetricAndAlertType(
                SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.BELOW))
                .thenReturn(Optional.empty());

        UUID existingId = UUID.randomUUID();
        ThresholdSetting existing = new ThresholdSetting();
        existing.setId(existingId);
        existing.setType(SensorType.TRAFFIC);
        existing.setMetric(Metric.TRAFFIC_DENSITY);
        existing.setThresholdValue(300f);
        existing.setAlertType(AlertType.ABOVE);
        existing.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(thresholdSettingRepository.findByTypeAndMetricAndAlertType(
                SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE))
                .thenReturn(Optional.of(existing));

        when(thresholdSettingRepository.save(existing)).thenReturn(existing);
        when(thresholdSettingRepository.findAll()).thenReturn(List.of(existing));

        ThresholdSettingResponse response = new ThresholdSettingResponse();
        response.setId(existingId);
        response.setType(SensorType.TRAFFIC);
        response.setMetric(Metric.TRAFFIC_DENSITY);
        response.setThresholdValue(400f);
        response.setAlertType(AlertType.ABOVE);
        response.setCreatedAt(existing.getCreatedAt());

        when(thresholdSettingMapper.toResponse(existing)).thenReturn(response);

        List<ThresholdSettingResponse> result = thresholdSettingService.upsert(List.of(request));

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(thresholdSettingRepository).save(any(ThresholdSetting.class));
        verify(thresholdSettingMapper, never()).toEntity(any());
    }

    @Test
    void upsert_throwsException_whenInvalidMetricForType() {
        ThresholdSettingRequest request = new ThresholdSettingRequest();
        request.setType(SensorType.TRAFFIC);
        request.setMetric(Metric.CO);
        request.setThresholdValue(10f);
        request.setAlertType(AlertType.ABOVE);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> thresholdSettingService.upsert(List.of(request)));

        assertEquals("invalid metric for this sensor type", ex.getMessage());
        verify(thresholdSettingRepository, never()).save(any());
    }

    @Test
    void upsert_throwsException_whenThresholdOutOfRange() {
        ThresholdSettingRequest request = new ThresholdSettingRequest();
        request.setType(SensorType.TRAFFIC);
        request.setMetric(Metric.TRAFFIC_DENSITY);
        request.setThresholdValue(600f);
        request.setAlertType(AlertType.ABOVE);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> thresholdSettingService.upsert(List.of(request)));

        assertEquals("thresholdValue out of valid range for this metric", ex.getMessage());
        verify(thresholdSettingRepository, never()).save(any());
    }

    @Test
    void upsert_throwsException_whenContradictoryThresholds() {
        ThresholdSettingRequest request1 = new ThresholdSettingRequest();
        request1.setType(SensorType.TRAFFIC);
        request1.setMetric(Metric.TRAFFIC_DENSITY);
        request1.setAlertType(AlertType.ABOVE);
        request1.setThresholdValue(100f);

        ThresholdSettingRequest request2 = new ThresholdSettingRequest();
        request2.setType(SensorType.TRAFFIC);
        request2.setMetric(Metric.TRAFFIC_DENSITY);
        request2.setAlertType(AlertType.BELOW);
        request2.setThresholdValue(200f);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> thresholdSettingService.upsert(List.of(request1, request2)));

        assertTrue(ex.getMessage().contains("Contradictory thresholds"));
        verify(thresholdSettingRepository, never()).save(any());
    }
}
