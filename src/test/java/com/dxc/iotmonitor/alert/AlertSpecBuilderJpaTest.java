package com.dxc.iotmonitor.alert;

import com.dxc.iotmonitor.alert.repository.AlertRepository;
import com.dxc.iotmonitor.alert.service.AlertSpecBuilder;
import com.dxc.iotmonitor.alert.dto.AlertFilterParams;
import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AlertSpecBuilderJpaTest {

    @Autowired
    private AlertRepository repository;

    @Autowired
    private UserRepository userRepository;

    private final AlertSpecBuilder specBuilder = new AlertSpecBuilder();

    private AlertData unreadAlert, readAlert, otherTypeAlert;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("alert-test@example.com")
                .firstName("Alert")
                .lastName("Test")
                .password("password123")
                .build();
        user = userRepository.save(user);

        unreadAlert = AlertData.builder()
                .user(user)
                .sensorType(SensorType.TRAFFIC)
                .metric(Metric.TRAFFIC_DENSITY)
                .alertType(AlertType.ABOVE)
                .location("CAIRO_RING_ROAD")
                .triggeredValue(480.0f)
                .thresholdValue(400.0f)
                .triggeredAt(LocalDateTime.of(2026, Month.JUNE, 1, 10, 0, 0))
                .readAt(null)
                .build();

        readAlert = AlertData.builder()
                .user(user)
                .sensorType(SensorType.TRAFFIC)
                .metric(Metric.AVG_SPEED)
                .alertType(AlertType.BELOW)
                .location("CAIRO_OCTOBER_BRIDGE")
                .triggeredValue(20.0f)
                .thresholdValue(30.0f)
                .triggeredAt(LocalDateTime.of(2026, Month.JUNE, 2, 10, 0, 0))
                .readAt(LocalDateTime.of(2026, Month.JUNE, 3, 10, 0, 0))
                .build();

        otherTypeAlert = AlertData.builder()
                .user(user)
                .sensorType(SensorType.AIR_POLLUTION)
                .metric(Metric.CO)
                .alertType(AlertType.ABOVE)
                .location("CAIRO_NASR_CITY")
                .triggeredValue(35.0f)
                .thresholdValue(30.0f)
                .triggeredAt(LocalDateTime.of(2026, Month.JUNE, 3, 10, 0, 0))
                .readAt(null)
                .build();

        repository.saveAll(List.of(unreadAlert, readAlert, otherTypeAlert));
    }

    @Test
    void filterBySensorType_returnsExactMatch() {
        var params = new AlertFilterParams(
                SensorType.TRAFFIC, null, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(2, results.size());
    }

    @Test
    void filterByMetric_returnsExactMatch() {
        var params = new AlertFilterParams(
                null, Metric.AVG_SPEED, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals(Metric.AVG_SPEED, results.get(0).getMetric());
    }

    @Test
    void filterByAlertType_returnsExactMatch() {
        var params = new AlertFilterParams(
                null, null, AlertType.BELOW, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals(AlertType.BELOW, results.get(0).getAlertType());
    }

    @Test
    void filterByLocation_returnsPartialMatch() {
        var params = new AlertFilterParams(
                null, null, null, "RING", null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals("CAIRO_RING_ROAD", results.get(0).getLocation());
    }

    @Test
    void filterByTriggeredAtRange_returnsInWindow() {
        var params = new AlertFilterParams(
                null, null, null, null,
                LocalDateTime.of(2026, Month.JUNE, 1, 0, 0, 0),
                LocalDateTime.of(2026, Month.JUNE, 2, 23, 59, 59),
                null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(2, results.size());
    }

    @Test
    void filterByReadTrue_returnsOnlyRead() {
        var params = new AlertFilterParams(
                null, null, null, null, null, null, Boolean.TRUE);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertNotNull(results.get(0).getReadAt());
    }

    @Test
    void filterByReadFalse_returnsOnlyUnread() {
        var params = new AlertFilterParams(
                null, null, null, null, null, null, Boolean.FALSE);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(2, results.size());
    }

    @Test
    void filterByCombined_returnsIntersection() {
        var params = new AlertFilterParams(
                SensorType.TRAFFIC, null, null, "CAIRO", null, null, Boolean.FALSE);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals("CAIRO_RING_ROAD", results.get(0).getLocation());
        assertNull(results.get(0).getReadAt());
    }

    @Test
    void filterWithNoFilters_returnsAll() {
        var params = new AlertFilterParams(
                null, null, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(3, results.size());
    }

    @Test
    void filterWithImpossibleCombination_returnsEmpty() {
        var params = new AlertFilterParams(
                SensorType.STREET_LIGHT, null, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(0, results.size());
    }
}
