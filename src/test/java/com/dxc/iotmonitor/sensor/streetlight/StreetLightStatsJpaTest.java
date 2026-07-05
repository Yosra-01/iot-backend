package com.dxc.iotmonitor.sensor.streetlight;

import com.dxc.iotmonitor.enums.LightStatus;
import com.dxc.iotmonitor.sensor.streetlight.model.StreetLightSensorData;
import com.dxc.iotmonitor.sensor.streetlight.repository.StreetLightSensorRepository;
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
class StreetLightStatsJpaTest {

    @Autowired
    private StreetLightSensorRepository repository;

    @BeforeEach
    void setUp() {
        repository.saveAll(List.of(
                StreetLightSensorData.builder()
                        .location("CAIRO_ZAMALEK")
                        .brightnessLevel(75)
                        .powerConsumption(1200.0f)
                        .status(LightStatus.ON)
                        .timestamp(LocalDateTime.of(2026, 6, 1, 10, 0, 0))
                        .build(),
                StreetLightSensorData.builder()
                        .location("CAIRO_ZAMALEK")
                        .brightnessLevel(85)
                        .powerConsumption(1300.0f)
                        .status(LightStatus.ON)
                        .timestamp(LocalDateTime.of(2026, 6, 1, 14, 0, 0))
                        .build(),
                StreetLightSensorData.builder()
                        .location("CAIRO_DOWNTOWN")
                        .brightnessLevel(20)
                        .powerConsumption(300.0f)
                        .status(LightStatus.OFF)
                        .timestamp(LocalDateTime.of(2026, 6, 2, 10, 0, 0))
                        .build(),
                StreetLightSensorData.builder()
                        .location("CAIRO_ZAMALEK")
                        .brightnessLevel(100)
                        .powerConsumption(2000.0f)
                        .status(LightStatus.ON)
                        .timestamp(LocalDateTime.of(2026, 6, 3, 10, 0, 0))
                        .build()
        ));
    }

    @Test
    void findStats_allRows_returnsCorrectAggregates() {
        var result = repository.findStats(null, null, null);

        assertNotNull(result);
        assertEquals(4L, result.getTotalReadings());
        assertEquals(70.0, result.getAvgBrightness(), 0.01);
        assertEquals(20, result.getMinBrightness());
        assertEquals(100, result.getMaxBrightness());
        assertEquals(1200.0, result.getAvgPowerConsumption(), 0.01);
        assertEquals(300.0f, result.getMinPowerConsumption(), 0.01);
        assertEquals(2000.0f, result.getMaxPowerConsumption(), 0.01);
    }

    @Test
    void findStats_withNoData_returnsNullAggregates() {
        repository.deleteAll();

        var result = repository.findStats(null, null, null);

        assertNotNull(result);
        assertEquals(0L, result.getTotalReadings());
        assertNull(result.getAvgBrightness());
        assertNull(result.getMinBrightness());
        assertNull(result.getMaxBrightness());
        assertNull(result.getAvgPowerConsumption());
        assertNull(result.getMinPowerConsumption());
        assertNull(result.getMaxPowerConsumption());
    }

    @Test
    void findStatusDistribution_allRows_returnsGroupedCounts() {
        var result = repository.findStatusDistribution(null, null, null);

        assertEquals(2, result.size());
        var map = new java.util.HashMap<LightStatus, Long>();
        result.forEach(p -> map.put(p.getStatus(), p.getCount()));
        assertEquals(3L, map.get(LightStatus.ON));
        assertEquals(1L, map.get(LightStatus.OFF));
    }

    @Test
    void findDailyAverages_allLocations_returnsGroupedByDate() {
        var from = LocalDateTime.of(2026, 6, 1, 0, 0, 0);
        var to = LocalDateTime.of(2026, 6, 3, 23, 59, 59);

        var result = repository.findDailyAverages(from, to, null);

        assertEquals(3, result.size());
        assertEquals(LocalDate.of(2026, 6, 1), result.get(0).getDate());
        assertEquals(80.0, result.get(0).getAvgBrightness(), 0.01);
        assertEquals(1250.0, result.get(0).getAvgPowerConsumption(), 0.01);
    }
}
