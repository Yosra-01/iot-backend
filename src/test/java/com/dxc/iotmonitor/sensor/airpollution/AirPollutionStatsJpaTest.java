package com.dxc.iotmonitor.sensor.airpollution;

import com.dxc.iotmonitor.enums.PollutionLevel;
import com.dxc.iotmonitor.sensor.airpollution.model.AirPollutionSensorData;
import com.dxc.iotmonitor.sensor.airpollution.repository.AirPollutionSensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AirPollutionStatsJpaTest {

    @Autowired
    private AirPollutionSensorRepository repository;

    @BeforeEach
    void setUp() {
        repository.saveAll(List.of(
                AirPollutionSensorData.builder()
                        .location("CAIRO_NASR_CITY")
                        .co(25.0f)
                        .ozone(180.0f)
                        .pm25(120.5f).pm10(200.0f).no2(30.0f).so2(15.0f)
                        .pollutionLevel(PollutionLevel.UNHEALTHY)
                        .timestamp(LocalDateTime.of(2026, Month.JUNE, 1, 10, 0, 0))
                        .build(),
                AirPollutionSensorData.builder()
                        .location("CAIRO_NASR_CITY")
                        .co(15.0f)
                        .ozone(120.0f)
                        .pm25(50.0f).pm10(60.0f).no2(15.0f).so2(5.0f)
                        .pollutionLevel(PollutionLevel.MODERATE)
                        .timestamp(LocalDateTime.of(2026, Month.JUNE, 1, 14, 0, 0))
                        .build(),
                AirPollutionSensorData.builder()
                        .location("CAIRO_MAADI")
                        .co(10.0f)
                        .ozone(60.0f)
                        .pm25(30.0f).pm10(40.0f).no2(10.0f).so2(3.0f)
                        .pollutionLevel(PollutionLevel.GOOD)
                        .timestamp(LocalDateTime.of(2026, Month.JUNE, 2, 10, 0, 0))
                        .build(),
                AirPollutionSensorData.builder()
                        .location("CAIRO_NASR_CITY")
                        .co(35.0f)
                        .ozone(200.0f)
                        .pm25(200.0f).pm10(300.0f).no2(100.0f).so2(150.0f)
                        .pollutionLevel(PollutionLevel.UNHEALTHY)
                        .timestamp(LocalDateTime.of(2026, Month.JUNE, 3, 10, 0, 0))
                        .build()
        ));
    }

    @Test
    void findStats_allRows_returnsCorrectAggregates() {
        var result = repository.findStats(null, null, null);

        assertNotNull(result);
        assertEquals(4L, result.getTotalReadings());
        assertEquals(21.25, result.getAvgCo(), 0.01);
        assertEquals(10.0f, result.getMinCo(), 0.01);
        assertEquals(35.0f, result.getMaxCo(), 0.01);
        assertEquals(140.0, result.getAvgOzone(), 0.01);
        assertEquals(60.0f, result.getMinOzone(), 0.01);
        assertEquals(200.0f, result.getMaxOzone(), 0.01);
    }

    @Test
    void findStats_withNoData_returnsNullAggregates() {
        repository.deleteAll();

        var result = repository.findStats(null, null, null);

        assertNotNull(result);
        assertEquals(0L, result.getTotalReadings());
        assertNull(result.getAvgCo());
        assertNull(result.getMinCo());
        assertNull(result.getMaxCo());
        assertNull(result.getAvgOzone());
        assertNull(result.getMinOzone());
        assertNull(result.getMaxOzone());
    }

    @Test
    void findPollutionLevelDistribution_allRows_returnsGroupedCounts() {
        var result = repository.findPollutionLevelDistribution(null, null, null);

        assertEquals(3, result.size());
        var map = new java.util.EnumMap<PollutionLevel, Long>(PollutionLevel.class);
        result.forEach(p -> map.put(p.getPollutionLevel(), p.getCount()));
        assertEquals(2L, map.get(PollutionLevel.UNHEALTHY));
        assertEquals(1L, map.get(PollutionLevel.MODERATE));
        assertEquals(1L, map.get(PollutionLevel.GOOD));
    }

    @Test
    void findDailyAverages_allLocations_returnsGroupedByDate() {
        var from = LocalDateTime.of(2026, Month.JUNE, 1, 0, 0, 0);
        var to = LocalDateTime.of(2026, Month.JUNE, 3, 23, 59, 59);

        var result = repository.findDailyAverages(from, to, null);

        assertEquals(3, result.size());
        assertEquals(LocalDate.of(2026, Month.JUNE, 1), result.get(0).getDate());
        assertEquals(20.0, result.get(0).getAvgCo(), 0.01);
        assertEquals(150.0, result.get(0).getAvgOzone(), 0.01);
    }
}
