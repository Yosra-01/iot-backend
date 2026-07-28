package com.dxc.iotmonitor.settings.service;

import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.settings.dto.SettingsRequest;
import com.dxc.iotmonitor.settings.dto.SettingsResponse;
import com.dxc.iotmonitor.settings.mapper.SettingsMapper;
import com.dxc.iotmonitor.settings.model.Settings;
import com.dxc.iotmonitor.settings.repository.SettingsRepository;
import com.dxc.iotmonitor.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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

    // --- upsert: happy paths ---

    @Test
    void upsert_savesNewRule_whenNotExists() {
        SettingsRequest request = request(SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE, 400f);
        stubNoOppositeThreshold(SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.BELOW);
        when(settingsRepository.findByUserAndTypeAndMetricAndAlertType(
                user, SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE))
                .thenReturn(Optional.empty());

        Settings mappedEntity = settingsEntity(null, SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE, 400f);
        when(settingsMapper.toEntity(request)).thenReturn(mappedEntity);

        UUID savedId = UUID.randomUUID();
        Settings savedEntity = settingsEntity(savedId, SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE, 400f);
        savedEntity.setCreatedAt(LocalDateTime.now());
        when(settingsRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(settingsRepository.findByUser(user)).thenReturn(List.of(savedEntity));
        when(settingsMapper.toResponse(savedEntity)).thenReturn(toResponse(savedEntity));

        List<SettingsResponse> result = settingsService.upsert(List.of(request), user);

        assertEquals(1, result.size());
        assertEquals(savedId, result.get(0).getId());
        assertEquals(user, mappedEntity.getUser());
        verify(settingsRepository).save(mappedEntity);
    }

    @Test
    void upsert_updatesExistingRule_whenExists() {
        SettingsRequest request = request(SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE, 400f);
        stubNoOppositeThreshold(SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.BELOW);

        UUID existingId = UUID.randomUUID();
        Settings existing = settingsEntity(existingId, SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE, 300f);
        existing.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(settingsRepository.findByUserAndTypeAndMetricAndAlertType(
                user, SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE))
                .thenReturn(Optional.of(existing));
        when(settingsRepository.save(existing)).thenReturn(existing);
        when(settingsRepository.findByUser(user)).thenReturn(List.of(existing));
        when(settingsMapper.toResponse(existing)).thenReturn(toResponse(existing));

        List<SettingsResponse> result = settingsService.upsert(List.of(request), user);

        assertEquals(1, result.size());
        assertEquals(400f, existing.getThresholdValue());
        verify(settingsRepository).save(existing);
        verify(settingsMapper, never()).toEntity(any());
    }

    @ParameterizedTest(name = "valid upsert {0}/{1} threshold={3}")
    @MethodSource("validMetricThresholds")
    void upsert_acceptsValidMetricAndThreshold(
            SensorType type, Metric metric, AlertType alertType, float threshold) {
        SettingsRequest request = request(type, metric, alertType, threshold);
        AlertType opposite = alertType == AlertType.ABOVE ? AlertType.BELOW : AlertType.ABOVE;
        stubNoOppositeThreshold(type, metric, opposite);
        when(settingsRepository.findByUserAndTypeAndMetricAndAlertType(user, type, metric, alertType))
                .thenReturn(Optional.empty());

        Settings mapped = settingsEntity(null, type, metric, alertType, threshold);
        when(settingsMapper.toEntity(request)).thenReturn(mapped);
        Settings saved = settingsEntity(UUID.randomUUID(), type, metric, alertType, threshold);
        when(settingsRepository.save(mapped)).thenReturn(saved);
        when(settingsRepository.findByUser(user)).thenReturn(List.of(saved));
        when(settingsMapper.toResponse(saved)).thenReturn(toResponse(saved));

        List<SettingsResponse> result = settingsService.upsert(List.of(request), user);

        assertEquals(1, result.size());
        verify(settingsRepository).save(mapped);
    }

    // --- upsert: metric / threshold validation ---

    @ParameterizedTest(name = "invalid metric {1} for {0}")
    @CsvSource({
            "TRAFFIC, CO",
            "TRAFFIC, OZONE",
            "AIR_POLLUTION, TRAFFIC_DENSITY",
            "AIR_POLLUTION, BRIGHTNESS_LEVEL",
            "STREET_LIGHT, CO",
            "STREET_LIGHT, AVG_SPEED"
    })
    void upsert_throws_whenInvalidMetricForType(SensorType type, Metric metric) {
        SettingsRequest request = request(type, metric, AlertType.ABOVE, 1f);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> settingsService.upsert(List.of(request), user));

        assertEquals("invalid metric for this sensor type", ex.getMessage());
        verify(settingsRepository, never()).save(any());
    }

    @ParameterizedTest(name = "out of range {1}={2}")
    @MethodSource("outOfRangeThresholds")
    void upsert_throws_whenThresholdOutOfRange(SensorType type, Metric metric, float threshold) {
        SettingsRequest request = request(type, metric, AlertType.ABOVE, threshold);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> settingsService.upsert(List.of(request), user));

        assertEquals("thresholdValue out of valid range for this metric", ex.getMessage());
        verify(settingsRepository, never()).save(any());
    }

    @Test
    void upsert_throws_whenThresholdValueIsNull() {
        SettingsRequest request = request(SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE, null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> settingsService.upsert(List.of(request), user));

        assertEquals("thresholdValue out of valid range for this metric", ex.getMessage());
        verify(settingsRepository, never()).save(any());
    }

    // --- upsert: contradictions ---

    @Test
    void upsert_throws_whenIncomingAboveAndBelowContradict() {
        SettingsRequest above = request(SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE, 100f);
        SettingsRequest below = request(SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.BELOW, 200f);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> settingsService.upsert(List.of(above, below), user));

        assertTrue(ex.getMessage().contains("Contradictory thresholds"));
        assertTrue(ex.getMessage().contains(Metric.TRAFFIC_DENSITY.name()));
        verify(settingsRepository, never()).save(any());
    }

    @Test
    void upsert_throws_whenIncomingBelowEqualsAbove() {
        SettingsRequest above = request(SensorType.TRAFFIC, Metric.AVG_SPEED, AlertType.ABOVE, 50f);
        SettingsRequest below = request(SensorType.TRAFFIC, Metric.AVG_SPEED, AlertType.BELOW, 50f);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> settingsService.upsert(List.of(above, below), user));

        assertTrue(ex.getMessage().contains("Contradictory thresholds"));
        verify(settingsRepository, never()).save(any());
    }

    @Test
    void upsert_throws_whenIncomingAboveConflictsWithDbBelow() {
        SettingsRequest above = request(SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE, 100f);
        Settings dbBelow = settingsEntity(UUID.randomUUID(), SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.BELOW, 150f);
        when(settingsRepository.findByUserAndTypeAndMetricAndAlertType(
                user, SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.BELOW))
                .thenReturn(Optional.of(dbBelow));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> settingsService.upsert(List.of(above), user));

        assertTrue(ex.getMessage().contains("Contradictory thresholds"));
        verify(settingsRepository, never()).save(any());
    }

    @Test
    void upsert_throws_whenIncomingBelowConflictsWithDbAbove() {
        SettingsRequest below = request(SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.BELOW, 200f);
        Settings dbAbove = settingsEntity(UUID.randomUUID(), SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE, 100f);
        when(settingsRepository.findByUserAndTypeAndMetricAndAlertType(
                user, SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE))
                .thenReturn(Optional.of(dbAbove));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> settingsService.upsert(List.of(below), user));

        assertTrue(ex.getMessage().contains("Contradictory thresholds"));
        verify(settingsRepository, never()).save(any());
    }

    @Test
    void upsert_succeeds_whenIncomingAboveCompatibleWithDbBelow() {
        SettingsRequest above = request(SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE, 300f);
        Settings dbBelow = settingsEntity(UUID.randomUUID(), SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.BELOW, 100f);
        when(settingsRepository.findByUserAndTypeAndMetricAndAlertType(
                user, SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.BELOW))
                .thenReturn(Optional.of(dbBelow));
        when(settingsRepository.findByUserAndTypeAndMetricAndAlertType(
                user, SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE))
                .thenReturn(Optional.empty());

        Settings mapped = settingsEntity(null, SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE, 300f);
        when(settingsMapper.toEntity(above)).thenReturn(mapped);
        Settings saved = settingsEntity(UUID.randomUUID(), SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE, 300f);
        when(settingsRepository.save(mapped)).thenReturn(saved);
        when(settingsRepository.findByUser(user)).thenReturn(List.of(dbBelow, saved));
        when(settingsMapper.toResponse(any(Settings.class))).thenAnswer(inv -> toResponse(inv.getArgument(0)));

        List<SettingsResponse> result = settingsService.upsert(List.of(above), user);

        assertEquals(2, result.size());
        verify(settingsRepository).save(mapped);
    }

    // --- findAll / flush ---

    @Test
    void findAll_returnsMappedSettings() {
        Settings entity = settingsEntity(UUID.randomUUID(), SensorType.TRAFFIC, Metric.AVG_SPEED, AlertType.BELOW, 40f);
        SettingsResponse response = toResponse(entity);
        when(settingsRepository.findByUser(user)).thenReturn(List.of(entity));
        when(settingsMapper.toResponse(entity)).thenReturn(response);

        List<SettingsResponse> result = settingsService.findAll(user);

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));
        verify(settingsRepository).findByUser(user);
    }

    @Test
    void findAll_returnsEmptyList_whenNoSettings() {
        when(settingsRepository.findByUser(user)).thenReturn(List.of());

        List<SettingsResponse> result = settingsService.findAll(user);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(settingsMapper, never()).toResponse(any());
    }

    @Test
    void flush_deletesAllSettings() {
        settingsService.flush();

        verify(settingsRepository).deleteAll();
    }

    // --- deleteById ---

    @Test
    void deleteById_deletes_whenUserOwnsSetting() {
        UUID id = UUID.randomUUID();
        Settings setting = settingsEntity(id, SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE, 100f);
        when(settingsRepository.findById(id)).thenReturn(Optional.of(setting));

        settingsService.deleteById(id.toString(), user);

        verify(settingsRepository).deleteById(id);
    }

    @Test
    void deleteById_throws_whenInvalidUuid() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> settingsService.deleteById("not-a-uuid", user));

        assertEquals("Invalid settings ID format.", ex.getMessage());
        verify(settingsRepository, never()).findById(any());
        verify(settingsRepository, never()).deleteById(any());
    }

    @Test
    void deleteById_throws_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(settingsRepository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> settingsService.deleteById(id.toString(), user));

        assertEquals("Setting not found.", ex.getMessage());
        verify(settingsRepository, never()).deleteById(any());
    }

    @Test
    void deleteById_throws_whenUserDoesNotOwnSetting() {
        UUID id = UUID.randomUUID();
        User otherUser = User.builder()
                .userId(UUID.randomUUID())
                .email("other@example.com")
                .firstName("Other")
                .lastName("User")
                .password("x")
                .build();
        Settings setting = settingsEntity(id, SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE, 100f);
        setting.setUser(otherUser);
        when(settingsRepository.findById(id)).thenReturn(Optional.of(setting));

        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> settingsService.deleteById(id.toString(), user));

        assertEquals("You do not have permission to delete this setting.", ex.getMessage());
        verify(settingsRepository, never()).deleteById(any());
    }

    // --- fixtures ---

    static Stream<Arguments> validMetricThresholds() {
        return Stream.of(
                Arguments.of(SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE, 0f),
                Arguments.of(SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, AlertType.ABOVE, 500f),
                Arguments.of(SensorType.TRAFFIC, Metric.AVG_SPEED, AlertType.BELOW, 0f),
                Arguments.of(SensorType.TRAFFIC, Metric.AVG_SPEED, AlertType.ABOVE, 120f),
                Arguments.of(SensorType.AIR_POLLUTION, Metric.CO, AlertType.ABOVE, 0f),
                Arguments.of(SensorType.AIR_POLLUTION, Metric.CO, AlertType.ABOVE, 50f),
                Arguments.of(SensorType.AIR_POLLUTION, Metric.OZONE, AlertType.BELOW, 0f),
                Arguments.of(SensorType.AIR_POLLUTION, Metric.OZONE, AlertType.ABOVE, 300f),
                Arguments.of(SensorType.STREET_LIGHT, Metric.BRIGHTNESS_LEVEL, AlertType.ABOVE, 0f),
                Arguments.of(SensorType.STREET_LIGHT, Metric.BRIGHTNESS_LEVEL, AlertType.ABOVE, 100f),
                Arguments.of(SensorType.STREET_LIGHT, Metric.POWER_CONSUMPTION, AlertType.BELOW, 0f),
                Arguments.of(SensorType.STREET_LIGHT, Metric.POWER_CONSUMPTION, AlertType.ABOVE, 5000f)
        );
    }

    static Stream<Arguments> outOfRangeThresholds() {
        return Stream.of(
                Arguments.of(SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, -1f),
                Arguments.of(SensorType.TRAFFIC, Metric.TRAFFIC_DENSITY, 501f),
                Arguments.of(SensorType.TRAFFIC, Metric.AVG_SPEED, -0.1f),
                Arguments.of(SensorType.TRAFFIC, Metric.AVG_SPEED, 120.1f),
                Arguments.of(SensorType.AIR_POLLUTION, Metric.CO, -1f),
                Arguments.of(SensorType.AIR_POLLUTION, Metric.CO, 51f),
                Arguments.of(SensorType.AIR_POLLUTION, Metric.OZONE, -1f),
                Arguments.of(SensorType.AIR_POLLUTION, Metric.OZONE, 301f),
                Arguments.of(SensorType.STREET_LIGHT, Metric.BRIGHTNESS_LEVEL, -1f),
                Arguments.of(SensorType.STREET_LIGHT, Metric.BRIGHTNESS_LEVEL, 101f),
                Arguments.of(SensorType.STREET_LIGHT, Metric.POWER_CONSUMPTION, -1f),
                Arguments.of(SensorType.STREET_LIGHT, Metric.POWER_CONSUMPTION, 5001f)
        );
    }

    private void stubNoOppositeThreshold(SensorType type, Metric metric, AlertType opposite) {
        when(settingsRepository.findByUserAndTypeAndMetricAndAlertType(user, type, metric, opposite))
                .thenReturn(Optional.empty());
    }

    private static SettingsRequest request(SensorType type, Metric metric, AlertType alertType, Float value) {
        SettingsRequest request = new SettingsRequest();
        request.setType(type);
        request.setMetric(metric);
        request.setAlertType(alertType);
        request.setThresholdValue(value);
        return request;
    }

    private Settings settingsEntity(
            UUID id, SensorType type, Metric metric, AlertType alertType, float threshold) {
        Settings settings = new Settings();
        settings.setId(id);
        settings.setUser(user);
        settings.setType(type);
        settings.setMetric(metric);
        settings.setAlertType(alertType);
        settings.setThresholdValue(threshold);
        return settings;
    }

    private static SettingsResponse toResponse(Settings settings) {
        SettingsResponse response = new SettingsResponse();
        response.setId(settings.getId());
        response.setType(settings.getType());
        response.setMetric(settings.getMetric());
        response.setAlertType(settings.getAlertType());
        response.setThresholdValue(settings.getThresholdValue());
        response.setCreatedAt(settings.getCreatedAt());
        return response;
    }
}
