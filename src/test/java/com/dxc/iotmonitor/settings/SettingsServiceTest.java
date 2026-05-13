package com.dxc.iotmonitor.settings;

import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.settings.dto.SettingsRequest;
import com.dxc.iotmonitor.settings.dto.SettingsResponse;
import com.dxc.iotmonitor.user.model.User;
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
class SettingsServiceTest {

    @Mock
    private SettingsRepository settingsRepository;

    @Mock
    private SettingsMapper settingsMapper;

    @InjectMocks
    private SettingsService settingsService;

    private final User user = User.builder()
            .userId(UUID.randomUUID())
            .email("u@example.com")
            .firstName("U")
            .lastName("Ser")
            .password("x")
            .build();

    @Test
    void upsert_savesNewRule_whenNotExists() {
        SettingsRequest request = new SettingsRequest();
        request.setType(SensorType.TRAFFIC);
        request.setMetric(Metric.TRAFFIC_DENSITY);
        request.setThresholdValue(400f);
        request.setAlertType(AlertType.ABOVE);

        when(settingsRepository.findByUserAndTypeAndMetricAndAlertType(
                user, SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.BELOW))
                .thenReturn(Optional.empty());
        when(settingsRepository.findByUserAndTypeAndMetricAndAlertType(
                user, SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE))
                .thenReturn(Optional.empty());

        Settings mappedEntity = new Settings();
        mappedEntity.setType(SensorType.TRAFFIC);
        mappedEntity.setMetric(Metric.TRAFFIC_DENSITY);
        mappedEntity.setThresholdValue(400f);
        mappedEntity.setAlertType(AlertType.ABOVE);

        when(settingsMapper.toEntity(request)).thenReturn(mappedEntity);

        UUID savedId = UUID.randomUUID();
        Settings savedEntity = new Settings();
        savedEntity.setId(savedId);
        savedEntity.setUser(user);
        savedEntity.setType(SensorType.TRAFFIC);
        savedEntity.setMetric(Metric.TRAFFIC_DENSITY);
        savedEntity.setThresholdValue(400f);
        savedEntity.setAlertType(AlertType.ABOVE);
        savedEntity.setCreatedAt(LocalDateTime.now());

        when(settingsRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(settingsRepository.findByUser(user)).thenReturn(List.of(savedEntity));

        SettingsResponse response = new SettingsResponse();
        response.setId(savedId);
        response.setType(SensorType.TRAFFIC);
        response.setMetric(Metric.TRAFFIC_DENSITY);
        response.setThresholdValue(400f);
        response.setAlertType(AlertType.ABOVE);
        response.setCreatedAt(savedEntity.getCreatedAt());

        when(settingsMapper.toResponse(savedEntity)).thenReturn(response);

        List<SettingsResponse> result = settingsService.upsert(List.of(request), user);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(settingsRepository).save(any(Settings.class));
    }

    @Test
    void upsert_updatesExistingRule_whenExists() {
        SettingsRequest request = new SettingsRequest();
        request.setType(SensorType.TRAFFIC);
        request.setMetric(Metric.TRAFFIC_DENSITY);
        request.setThresholdValue(400f);
        request.setAlertType(AlertType.ABOVE);

        when(settingsRepository.findByUserAndTypeAndMetricAndAlertType(
                user, SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.BELOW))
                .thenReturn(Optional.empty());

        UUID existingId = UUID.randomUUID();
        Settings existing = new Settings();
        existing.setId(existingId);
        existing.setUser(user);
        existing.setType(SensorType.TRAFFIC);
        existing.setMetric(Metric.TRAFFIC_DENSITY);
        existing.setThresholdValue(300f);
        existing.setAlertType(AlertType.ABOVE);
        existing.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(settingsRepository.findByUserAndTypeAndMetricAndAlertType(
                user, SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE))
                .thenReturn(Optional.of(existing));

        when(settingsRepository.save(existing)).thenReturn(existing);
        when(settingsRepository.findByUser(user)).thenReturn(List.of(existing));

        SettingsResponse response = new SettingsResponse();
        response.setId(existingId);
        response.setType(SensorType.TRAFFIC);
        response.setMetric(Metric.TRAFFIC_DENSITY);
        response.setThresholdValue(400f);
        response.setAlertType(AlertType.ABOVE);
        response.setCreatedAt(existing.getCreatedAt());

        when(settingsMapper.toResponse(existing)).thenReturn(response);

        List<SettingsResponse> result = settingsService.upsert(List.of(request), user);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(settingsRepository).save(any(Settings.class));
        verify(settingsMapper, never()).toEntity(any());
    }

    @Test
    void upsert_throwsException_whenInvalidMetricForType() {
        SettingsRequest request = new SettingsRequest();
        request.setType(SensorType.TRAFFIC);
        request.setMetric(Metric.CO);
        request.setThresholdValue(10f);
        request.setAlertType(AlertType.ABOVE);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> settingsService.upsert(List.of(request), user));

        assertEquals("invalid metric for this sensor type", ex.getMessage());
        verify(settingsRepository, never()).save(any());
    }

    @Test
    void upsert_throwsException_whenThresholdOutOfRange() {
        SettingsRequest request = new SettingsRequest();
        request.setType(SensorType.TRAFFIC);
        request.setMetric(Metric.TRAFFIC_DENSITY);
        request.setThresholdValue(600f);
        request.setAlertType(AlertType.ABOVE);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> settingsService.upsert(List.of(request), user));

        assertEquals("thresholdValue out of valid range for this metric", ex.getMessage());
        verify(settingsRepository, never()).save(any());
    }

    @Test
    void upsert_throwsException_whenContradictoryThresholds() {
        SettingsRequest request1 = new SettingsRequest();
        request1.setType(SensorType.TRAFFIC);
        request1.setMetric(Metric.TRAFFIC_DENSITY);
        request1.setAlertType(AlertType.ABOVE);
        request1.setThresholdValue(100f);

        SettingsRequest request2 = new SettingsRequest();
        request2.setType(SensorType.TRAFFIC);
        request2.setMetric(Metric.TRAFFIC_DENSITY);
        request2.setAlertType(AlertType.BELOW);
        request2.setThresholdValue(200f);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> settingsService.upsert(List.of(request1, request2), user));

        assertTrue(ex.getMessage().contains("Contradictory thresholds"));
        verify(settingsRepository, never()).save(any());
    }
}
