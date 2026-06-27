package com.dxc.iotmonitor.sensor.traffic;

import com.dxc.iotmonitor.enums.CongestionLevel;
import com.dxc.iotmonitor.sensor.traffic.model.TrafficSensorData;
import com.dxc.iotmonitor.sensor.traffic.repository.TrafficSensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TrafficStatsJpaTest {

    @Autowired
    private TrafficSensorRepository repository;

    @BeforeEach
    void setUp() {
        TrafficSensorData row1 = TrafficSensorData.builder()
                .location("CAIRO_RING_ROAD")
                .trafficDensity(200)
                .avgSpeed(60.0f)
                .congestionLevel(CongestionLevel.MODERATE)
                .timestamp(LocalDateTime.of(2026, 6, 1, 10, 0, 0))
                .build();

        TrafficSensorData row2 = TrafficSensorData.builder()
                .location("CAIRO_RING_ROAD")
                .trafficDensity(400)
                .avgSpeed(30.0f)
                .congestionLevel(CongestionLevel.HIGH)
                .timestamp(LocalDateTime.of(2026, 6, 1, 14, 0, 0))
                .build();

        TrafficSensorData row3 = TrafficSensorData.builder()
                .location("CAIRO_OCTOBER_BRIDGE")
                .trafficDensity(100)
                .avgSpeed(80.0f)
                .congestionLevel(CongestionLevel.LOW)
                .timestamp(LocalDateTime.of(2026, 6, 2, 10, 0, 0))
                .build();

        TrafficSensorData row4 = TrafficSensorData.builder()
                .location("CAIRO_RING_ROAD")
                .trafficDensity(300)
                .avgSpeed(50.0f)
                .congestionLevel(CongestionLevel.MODERATE)
                .timestamp(LocalDateTime.of(2026, 6, 3, 10, 0, 0))
                .build();

        repository.saveAll(List.of(row1, row2, row3, row4));
    }

    @Test
    void findStats_allRows_returnsCorrectAggregates() {
        var result = repository.findStats(null, null, null);

        assertNotNull(result);
        assertEquals(4L, result.getTotalReadings());
        assertEquals(250.0, result.getAvgTrafficDensity(), 0.01);
        assertEquals(100, result.getMinTrafficDensity());
        assertEquals(400, result.getMaxTrafficDensity());
        assertEquals(55.0, result.getAvgSpeed(), 0.01);
        assertEquals(30.0f, result.getMinSpeed(), 0.01);
        assertEquals(80.0f, result.getMaxSpeed(), 0.01);
    }

    @Test
    void findStats_withLocationFilter_returnsScoped() {
        var result = repository.findStats(null, null, "CAIRO_RING_ROAD");

        assertNotNull(result);
        assertEquals(3L, result.getTotalReadings());
        assertEquals(300.0, result.getAvgTrafficDensity(), 0.01);
        assertEquals(200, result.getMinTrafficDensity());
        assertEquals(400, result.getMaxTrafficDensity());
        assertEquals(46.67, result.getAvgSpeed(), 0.1);
        assertEquals(30.0f, result.getMinSpeed(), 0.01);
        assertEquals(60.0f, result.getMaxSpeed(), 0.01);
    }

    @Test
    void findStats_withNoData_returnsNullAggregates() {
        repository.deleteAll();

        var result = repository.findStats(null, null, null);

        assertNotNull(result);
        assertEquals(0L, result.getTotalReadings());
        assertNull(result.getAvgTrafficDensity());
        assertNull(result.getMinTrafficDensity());
        assertNull(result.getMaxTrafficDensity());
        assertNull(result.getAvgSpeed());
        assertNull(result.getMinSpeed());
        assertNull(result.getMaxSpeed());
    }

    @Test
    void findCongestionLevelDistribution_allRows_returnsGroupedCounts() {
        var result = repository.findCongestionLevelDistribution(null, null, null);

        assertEquals(3, result.size());
        var map = new java.util.HashMap<CongestionLevel, Long>();
        result.forEach(p -> map.put(p.getCongestionLevel(), p.getCount()));
        assertEquals(2L, map.get(CongestionLevel.MODERATE));
        assertEquals(1L, map.get(CongestionLevel.HIGH));
        assertEquals(1L, map.get(CongestionLevel.LOW));
    }

    @Test
    void findCongestionLevelDistribution_withLocationFilter_returnsScoped() {
        var result = repository.findCongestionLevelDistribution(null, null, "CAIRO_OCTOBER_BRIDGE");

        assertEquals(1, result.size());
        assertEquals(CongestionLevel.LOW, result.get(0).getCongestionLevel());
        assertEquals(1L, result.get(0).getCount());
    }

    @Test
    void findDailyAverages_allLocations_returnsGroupedByDate() {
        var from = LocalDateTime.of(2026, 6, 1, 0, 0, 0);
        var to = LocalDateTime.of(2026, 6, 3, 23, 59, 59);

        var result = repository.findDailyAverages(from, to, null);

        assertEquals(3, result.size());
        assertEquals(LocalDate.of(2026, 6, 1), result.get(0).getDate());
        assertEquals(300.0, result.get(0).getAvgTrafficDensity(), 0.01);
        assertEquals(45.0, result.get(0).getAvgSpeed(), 0.01);
        assertEquals(LocalDate.of(2026, 6, 2), result.get(1).getDate());
        assertEquals(LocalDate.of(2026, 6, 3), result.get(2).getDate());
    }

    @Test
    void findDailyAverages_withLocationFilter_returnsSparseSeries() {
        var from = LocalDateTime.of(2026, 6, 1, 0, 0, 0);
        var to = LocalDateTime.of(2026, 6, 3, 23, 59, 59);

        var result = repository.findDailyAverages(from, to, "CAIRO_RING_ROAD");

        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2026, 6, 1), result.get(0).getDate());
        assertEquals(LocalDate.of(2026, 6, 3), result.get(1).getDate());
    }

    @Test
    void findDailyAverages_noDataInRange_returnsEmpty() {
        var from = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        var to = LocalDateTime.of(2025, 1, 2, 0, 0, 0);

        var result = repository.findDailyAverages(from, to, null);

        assertEquals(0, result.size());
    }
}
