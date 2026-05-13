package com.dxc.iotmonitor.alert.service;

import com.dxc.iotmonitor.alert.AlertData;
import com.dxc.iotmonitor.alert.dto.response.AlertResponse;
import com.dxc.iotmonitor.alert.mapper.AlertMapper;
import com.dxc.iotmonitor.alert.repository.AlertRepository;
import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.settings.Settings;
import com.dxc.iotmonitor.settings.SettingsRepository;
import com.dxc.iotmonitor.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    void findAll_returnsMappedList() {
        AlertData alertData = new AlertData();
        alertData.setId(UUID.randomUUID());
        AlertResponse response = new AlertResponse();
        response.setId(alertData.getId());

        when(alertRepository.findByUserOrderByTriggeredAtDesc(user)).thenReturn(List.of(alertData));
        when(alertMapper.toResponse(alertData)).thenReturn(response);

        List<AlertResponse> result = alertService.findAll(user);

        assertEquals(1, result.size());
    }

    @Test
    void findById_found_returnsMappedResponse() {
        AlertData alertData = new AlertData();
        alertData.setId(UUID.randomUUID());
        AlertResponse response = new AlertResponse();
        response.setId(alertData.getId());

        when(alertRepository.findById(any(UUID.class))).thenReturn(Optional.of(alertData));
        when(alertMapper.toResponse(alertData)).thenReturn(response);

        AlertResponse result = alertService.findById(alertData.getId());

        assertNotNull(result);
    }

    @Test
    void findById_notFound_throwsResourceNotFoundException() {
        when(alertRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> alertService.findById(UUID.randomUUID()));

        assertEquals("Alert not found.", ex.getMessage());
    }

    @Test
    void count_returnsCount() {
        when(alertRepository.countByUser(user)).thenReturn(5L);

        assertEquals(5L, alertService.count(user));
    }

    @Test
    void deleteById_found_deletesSuccessfully() {
        UUID id = UUID.randomUUID();
        AlertData alertData = new AlertData();
        alertData.setId(id);

        when(alertRepository.findById(id)).thenReturn(Optional.of(alertData));

        alertService.deleteById(id);

        verify(alertRepository, times(1)).deleteById(id);
    }

    @Test
    void deleteById_notFound_throwsResourceNotFoundException() {
        when(alertRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> alertService.deleteById(UUID.randomUUID()));

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
                user);

        verify(alertRepository, times(1)).save(any(AlertData.class));
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
                user);

        verify(alertRepository, never()).save(any(AlertData.class));
    }
}
