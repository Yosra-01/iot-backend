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
import com.dxc.iotmonitor.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertMapper alertMapper;

    @Mock
    private SettingsRepository settingsRepository;

    @Mock
    private AlertSpecBuilder alertSpecBuilder;

    @InjectMocks
    private AlertService alertService;

    private final User user = User.builder()
            .userId(UUID.randomUUID())
            .email("u@example.com")
            .firstName("U")
            .lastName("Ser")
            .password("x")
            .build();

    @Test
    void findFiltered_returnsPaginatedResults() {
        AlertFilterParams filters = new AlertFilterParams(
                SensorType.TRAFFIC, null, null, null, null, null, Boolean.FALSE);
        Pageable pageable = PageRequest.of(0, 10);

        AlertData alertData = new AlertData();
        alertData.setId(UUID.randomUUID());
        Page<AlertData> mockPage = new PageImpl<>(List.of(alertData));
        AlertResponse response = new AlertResponse();
        response.setId(alertData.getId());

        Specification<AlertData> mockSpec = (root, query, cb) -> cb.conjunction();
        when(alertSpecBuilder.build(any(AlertFilterParams.class))).thenReturn(mockSpec);
        when(alertRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);
        when(alertMapper.toResponse(alertData)).thenReturn(response);

        Page<AlertResponse> result = alertService.findFiltered(filters, pageable, user);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findById_found_returnsMappedResponse() {
        AlertData alertData = new AlertData();
        alertData.setId(UUID.randomUUID());
        alertData.setUser(user);
        AlertResponse response = new AlertResponse();
        response.setId(alertData.getId());

        when(alertRepository.findById(any(UUID.class))).thenReturn(Optional.of(alertData));
        when(alertMapper.toResponse(alertData)).thenReturn(response);

        AlertResponse result = alertService.findById(alertData.getId(), user);

        assertNotNull(result);
    }

    @Test
    void findById_notOwned_throwsAccessDeniedException() {
        User otherUser = User.builder()
                .userId(UUID.randomUUID())
                .email("other@example.com")
                .firstName("O")
                .lastName("Ther")
                .password("x")
                .build();

        AlertData alertData = new AlertData();
        alertData.setId(UUID.randomUUID());
        alertData.setUser(otherUser);

        when(alertRepository.findById(alertData.getId())).thenReturn(Optional.of(alertData));

        UUID alertId = alertData.getId();
        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> alertService.findById(alertId, user));

        assertTrue(ex.getMessage().contains("permission"));
    }

    @Test
    void findById_notFound_throwsResourceNotFoundException() {
        when(alertRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        UUID id = UUID.randomUUID();
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> alertService.findById(id, user));

        assertEquals("Alert not found.", ex.getMessage());
    }

    @Test
    void count_withFilters_returnsScopedCount() {
        AlertFilterParams filters = new AlertFilterParams(
                null, null, null, null, null, null, Boolean.FALSE);

        Specification<AlertData> mockSpec = (root, query, cb) -> cb.conjunction();
        when(alertSpecBuilder.build(any(AlertFilterParams.class))).thenReturn(mockSpec);
        when(alertRepository.count(any(Specification.class))).thenReturn(5L);

        long result = alertService.count(filters, user);

        assertEquals(5L, result);
    }

    @Test
    void deleteById_found_deletesSuccessfully() {
        UUID id = UUID.randomUUID();
        AlertData alertData = new AlertData();
        alertData.setId(id);
        alertData.setUser(user);

        when(alertRepository.findById(id)).thenReturn(Optional.of(alertData));

        alertService.deleteById(id, user);

        verify(alertRepository, times(1)).deleteById(id);
    }

    @Test
    void deleteById_notOwned_throwsAccessDeniedException() {
        User otherUser = User.builder()
                .userId(UUID.randomUUID())
                .email("other@example.com")
                .firstName("O")
                .lastName("Ther")
                .password("x")
                .build();

        UUID id = UUID.randomUUID();
        AlertData alertData = new AlertData();
        alertData.setId(id);
        alertData.setUser(otherUser);

        when(alertRepository.findById(id)).thenReturn(Optional.of(alertData));

        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> alertService.deleteById(id, user));

        assertTrue(ex.getMessage().contains("permission"));
        verify(alertRepository, never()).deleteById(id);
    }

    @Test
    void deleteById_notFound_throwsResourceNotFoundException() {
        when(alertRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        UUID id = UUID.randomUUID();
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> alertService.deleteById(id, user));

        assertEquals("Alert not found.", ex.getMessage());
    }

    @Test
    void markAsRead_unread_setsReadAt() {
        UUID id = UUID.randomUUID();
        AlertData alertData = new AlertData();
        alertData.setId(id);
        alertData.setUser(user);
        alertData.setReadAt(null);

        when(alertRepository.findById(id)).thenReturn(Optional.of(alertData));

        alertService.markAsRead(id, user);

        assertNotNull(alertData.getReadAt());
        verify(alertRepository, times(1)).save(alertData);
    }

    @Test
    void markAsRead_alreadyRead_isIdempotent() {
        UUID id = UUID.randomUUID();
        LocalDateTime originalReadAt = LocalDateTime.of(2026, Month.JUNE, 1, 10, 0, 0);
        AlertData alertData = new AlertData();
        alertData.setId(id);
        alertData.setUser(user);
        alertData.setReadAt(originalReadAt);

        when(alertRepository.findById(id)).thenReturn(Optional.of(alertData));

        alertService.markAsRead(id, user);

        assertEquals(originalReadAt, alertData.getReadAt());
        verify(alertRepository, never()).save(any(AlertData.class));
    }

    @Test
    void markAsRead_notOwned_throwsAccessDeniedException() {
        User otherUser = User.builder()
                .userId(UUID.randomUUID())
                .email("other@example.com")
                .firstName("O")
                .lastName("Ther")
                .password("x")
                .build();

        UUID id = UUID.randomUUID();
        AlertData alertData = new AlertData();
        alertData.setId(id);
        alertData.setUser(otherUser);

        when(alertRepository.findById(id)).thenReturn(Optional.of(alertData));

        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> alertService.markAsRead(id, user));

        assertTrue(ex.getMessage().contains("permission"));
        verify(alertRepository, never()).save(any(AlertData.class));
    }

    @Test
    void markAsRead_notFound_throwsResourceNotFoundException() {
        when(alertRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        UUID id = UUID.randomUUID();
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> alertService.markAsRead(id, user));

        assertEquals("Alert not found.", ex.getMessage());
    }

    @Test
    void checkAndTrigger_aboveBreach_savesAlert() {
        Settings setting = new Settings();
        setting.setType(SensorType.TRAFFIC);
        setting.setMetric(Metric.TRAFFIC_DENSITY);
        setting.setAlertType(AlertType.ABOVE);
        setting.setThresholdValue(400.0f);

        when(settingsRepository.findByUser(user)).thenReturn(List.of(setting));

        alertService.checkAndTrigger(
                SensorType.TRAFFIC,
                Map.of(Metric.TRAFFIC_DENSITY, 480.0f),
                "CAIRO_RING_ROAD",
                user,
                UUID.randomUUID());

        verify(alertRepository, times(1)).saveAll(anyList());
    }

    @Test
    void checkAndTrigger_noBreach_doesNotSave() {
        Settings setting = new Settings();
        setting.setType(SensorType.TRAFFIC);
        setting.setMetric(Metric.TRAFFIC_DENSITY);
        setting.setAlertType(AlertType.ABOVE);
        setting.setThresholdValue(400.0f);

        when(settingsRepository.findByUser(user)).thenReturn(List.of(setting));

        alertService.checkAndTrigger(
                SensorType.TRAFFIC,
                Map.of(Metric.TRAFFIC_DENSITY, 300.0f),
                "CAIRO_RING_ROAD",
                user,
                UUID.randomUUID());

        verify(alertRepository, never()).saveAll(anyList());
    }

    @Test
    void checkAndTrigger_belowBreach_savesAlert() {
        Settings setting = new Settings();
        setting.setType(SensorType.TRAFFIC);
        setting.setMetric(Metric.TRAFFIC_DENSITY);
        setting.setAlertType(AlertType.BELOW);
        setting.setThresholdValue(200.0f);

        when(settingsRepository.findByUser(user)).thenReturn(List.of(setting));

        alertService.checkAndTrigger(
                SensorType.TRAFFIC,
                Map.of(Metric.TRAFFIC_DENSITY, 100.0f),
                "CAIRO_RING_ROAD",
                user,
                UUID.randomUUID());

        verify(alertRepository, times(1)).saveAll(anyList());
    }

    @Test
    void checkAndTrigger_belowNoBreach_valueEqualsThreshold_doesNotSave() {
        Settings setting = new Settings();
        setting.setType(SensorType.TRAFFIC);
        setting.setMetric(Metric.TRAFFIC_DENSITY);
        setting.setAlertType(AlertType.BELOW);
        setting.setThresholdValue(200.0f);

        when(settingsRepository.findByUser(user)).thenReturn(List.of(setting));

        alertService.checkAndTrigger(
                SensorType.TRAFFIC,
                Map.of(Metric.TRAFFIC_DENSITY, 200.0f),
                "CAIRO_RING_ROAD",
                user,
                UUID.randomUUID());

        verify(alertRepository, never()).saveAll(anyList());
    }

    @Test
    void checkAndTrigger_aboveBoundary_valueEqualsThreshold_doesNotSave() {
        Settings setting = new Settings();
        setting.setType(SensorType.TRAFFIC);
        setting.setMetric(Metric.TRAFFIC_DENSITY);
        setting.setAlertType(AlertType.ABOVE);
        setting.setThresholdValue(400.0f);

        when(settingsRepository.findByUser(user)).thenReturn(List.of(setting));

        alertService.checkAndTrigger(
                SensorType.TRAFFIC,
                Map.of(Metric.TRAFFIC_DENSITY, 400.0f),
                "CAIRO_RING_ROAD",
                user,
                UUID.randomUUID());

        verify(alertRepository, never()).saveAll(anyList());
    }

    @Test
    void checkAndTrigger_typeMismatch_doesNotSave() {
        Settings setting = new Settings();
        setting.setType(SensorType.AIR_POLLUTION);
        setting.setMetric(Metric.CO);
        setting.setAlertType(AlertType.ABOVE);
        setting.setThresholdValue(30.0f);

        when(settingsRepository.findByUser(user)).thenReturn(List.of(setting));

        alertService.checkAndTrigger(
                SensorType.TRAFFIC,
                Map.of(Metric.CO, 50.0f),
                "CAIRO_RING_ROAD",
                user,
                UUID.randomUUID());

        verify(alertRepository, never()).saveAll(anyList());
    }

    @Test
    void checkAndTrigger_metricNotInValues_doesNotSave() {
        Settings setting = new Settings();
        setting.setType(SensorType.TRAFFIC);
        setting.setMetric(Metric.TRAFFIC_DENSITY);
        setting.setAlertType(AlertType.ABOVE);
        setting.setThresholdValue(400.0f);

        when(settingsRepository.findByUser(user)).thenReturn(List.of(setting));

        alertService.checkAndTrigger(
                SensorType.TRAFFIC,
                Map.of(Metric.AVG_SPEED, 60.0f),
                "CAIRO_RING_ROAD",
                user,
                UUID.randomUUID());

        verify(alertRepository, never()).saveAll(anyList());
    }

    @Test
    void checkAndTrigger_nullValueInMap_doesNotSave() {
        Settings setting = new Settings();
        setting.setType(SensorType.TRAFFIC);
        setting.setMetric(Metric.TRAFFIC_DENSITY);
        setting.setAlertType(AlertType.ABOVE);
        setting.setThresholdValue(400.0f);

        when(settingsRepository.findByUser(user)).thenReturn(List.of(setting));

        Map<Metric, Float> values = new EnumMap<>(Metric.class);
        values.put(Metric.TRAFFIC_DENSITY, null);

        alertService.checkAndTrigger(
                SensorType.TRAFFIC,
                values,
                "CAIRO_RING_ROAD",
                user,
                UUID.randomUUID());

        verify(alertRepository, never()).saveAll(anyList());
    }

    @Test
    void checkAndTrigger_emptySettings_doesNotSave() {
        when(settingsRepository.findByUser(user)).thenReturn(List.of());

        alertService.checkAndTrigger(
                SensorType.TRAFFIC,
                Map.of(Metric.TRAFFIC_DENSITY, 480.0f),
                "CAIRO_RING_ROAD",
                user,
                UUID.randomUUID());

        verify(alertRepository, never()).saveAll(anyList());
    }

    @Test
    void checkAndTrigger_multipleSettingsPartialBreach_savesOnlyBreaching() {
        Settings settingBreach = new Settings();
        settingBreach.setType(SensorType.TRAFFIC);
        settingBreach.setMetric(Metric.TRAFFIC_DENSITY);
        settingBreach.setAlertType(AlertType.ABOVE);
        settingBreach.setThresholdValue(400.0f);

        Settings settingNoBreach = new Settings();
        settingNoBreach.setType(SensorType.TRAFFIC);
        settingNoBreach.setMetric(Metric.TRAFFIC_DENSITY);
        settingNoBreach.setAlertType(AlertType.ABOVE);
        settingNoBreach.setThresholdValue(500.0f);

        when(settingsRepository.findByUser(user)).thenReturn(List.of(settingBreach, settingNoBreach));

        alertService.checkAndTrigger(
                SensorType.TRAFFIC,
                Map.of(Metric.TRAFFIC_DENSITY, 480.0f),
                "CAIRO_RING_ROAD",
                user,
                UUID.randomUUID());

        verify(alertRepository, times(1)).saveAll(anyList());
    }
}
