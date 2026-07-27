package com.dxc.iotmonitor.sensor.traffic;

import com.dxc.iotmonitor.enums.CongestionLevel;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficFilterParams;
import com.dxc.iotmonitor.sensor.traffic.model.TrafficSensorData;
import com.dxc.iotmonitor.sensor.traffic.repository.TrafficSensorRepository;
import com.dxc.iotmonitor.sensor.traffic.service.TrafficSpecBuilder;
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

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TrafficSpecBuilderJpaTest {

    @Autowired
    private TrafficSensorRepository repository;

    private final TrafficSpecBuilder specBuilder = new TrafficSpecBuilder();

    private TrafficSensorData row1, row2, row3;

    @BeforeEach
    void setUp() {
        row1 = TrafficSensorData.builder()
                .location("CAIRO_RING_ROAD")
                .trafficDensity(200)
                .avgSpeed(60.0f)
                .congestionLevel(CongestionLevel.MODERATE)
                .timestamp(LocalDateTime.of(2026, Month.JUNE, 1, 10, 0, 0))
                .build();

        row2 = TrafficSensorData.builder()
                .location("CAIRO_OCTOBER_BRIDGE")
                .trafficDensity(400)
                .avgSpeed(30.0f)
                .congestionLevel(CongestionLevel.HIGH)
                .timestamp(LocalDateTime.of(2026, Month.JUNE, 2, 10, 0, 0))
                .build();

        row3 = TrafficSensorData.builder()
                .location("CAIRO_SALAH_SALEM_ROAD")
                .trafficDensity(100)
                .avgSpeed(80.0f)
                .congestionLevel(CongestionLevel.LOW)
                .timestamp(LocalDateTime.of(2026, Month.JUNE, 3, 10, 0, 0))
                .build();

        repository.saveAll(List.of(row1, row2, row3));
    }

    @Test
    void filterByLocation_returnsPartialMatch() {
        var params = new TrafficFilterParams(
                "RING", null, null, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals("CAIRO_RING_ROAD", results.get(0).getLocation());
    }

    @Test
    void filterByDensityRange_returnsInRange() {
        var params = new TrafficFilterParams(
                null, 150, 350, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals(200, results.get(0).getTrafficDensity());
    }

    @Test
    void filterBySpeedRange_returnsInRange() {
        var params = new TrafficFilterParams(
                null, null, null, 50.0f, 70.0f, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals(60.0f, results.get(0).getAvgSpeed(), 0.01);
    }

    @Test
    void filterByCongestionLevel_returnsExactMatch() {
        var params = new TrafficFilterParams(
                null, null, null, null, null, CongestionLevel.HIGH, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals(CongestionLevel.HIGH, results.get(0).getCongestionLevel());
    }

    @Test
    void filterByTimestampRange_returnsInWindow() {
        var params = new TrafficFilterParams(
                null, null, null, null, null, null,
                LocalDateTime.of(2026, Month.JUNE, 2, 0, 0, 0),
                LocalDateTime.of(2026, Month.JUNE, 3, 23, 59, 59));
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(2, results.size());
    }

    @Test
    void filterByCombinedFilters_returnsIntersection() {
        var params = new TrafficFilterParams(
                "CAIRO", 150, null, null, null, CongestionLevel.MODERATE, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals("CAIRO_RING_ROAD", results.get(0).getLocation());
        assertEquals(CongestionLevel.MODERATE, results.get(0).getCongestionLevel());
    }

    @Test
    void filterWithNoFilters_returnsAll() {
        var params = new TrafficFilterParams(
                null, null, null, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(3, results.size());
    }

    @Test
    void filterWithImpossibleRange_returnsEmpty() {
        var params = new TrafficFilterParams(
                null, 600, null, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(0, results.size());
    }
}
