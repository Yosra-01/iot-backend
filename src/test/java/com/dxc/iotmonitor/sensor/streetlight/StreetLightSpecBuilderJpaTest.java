package com.dxc.iotmonitor.sensor.streetlight;

import com.dxc.iotmonitor.enums.LightStatus;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightFilterParams;
import com.dxc.iotmonitor.sensor.streetlight.model.StreetLightSensorData;
import com.dxc.iotmonitor.sensor.streetlight.repository.StreetLightSensorRepository;
import com.dxc.iotmonitor.sensor.streetlight.service.StreetLightSpecBuilder;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StreetLightSpecBuilderJpaTest {

    @Autowired
    private StreetLightSensorRepository repository;

    private final StreetLightSpecBuilder specBuilder = new StreetLightSpecBuilder();

    private StreetLightSensorData row1, row2, row3;

    @BeforeEach
    void setUp() {
        row1 = StreetLightSensorData.builder()
                .location("CAIRO_ZAMALEK")
                .brightnessLevel(75)
                .powerConsumption(1200.0f)
                .status(LightStatus.ON)
                .timestamp(LocalDateTime.of(2026, Month.JUNE, 1, 10, 0, 0))
                .build();

        row2 = StreetLightSensorData.builder()
                .location("CAIRO_DOWNTOWN")
                .brightnessLevel(20)
                .powerConsumption(300.0f)
                .status(LightStatus.OFF)
                .timestamp(LocalDateTime.of(2026, Month.JUNE, 2, 10, 0, 0))
                .build();

        row3 = StreetLightSensorData.builder()
                .location("CAIRO_NEW_CAIRO")
                .brightnessLevel(100)
                .powerConsumption(4500.0f)
                .status(LightStatus.ON)
                .timestamp(LocalDateTime.of(2026, Month.JUNE, 3, 10, 0, 0))
                .build();

        repository.saveAll(List.of(row1, row2, row3));
    }

    @Test
    void filterByLocation_returnsPartialMatch() {
        var params = new StreetLightFilterParams(
                "ZAMALEK", null, null, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals("CAIRO_ZAMALEK", results.get(0).getLocation());
    }

    @Test
    void filterByBrightnessRange_returnsInRange() {
        var params = new StreetLightFilterParams(
                null, 50, 80, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals(75, results.get(0).getBrightnessLevel());
    }

    @Test
    void filterByPowerRange_returnsInRange() {
        var params = new StreetLightFilterParams(
                null, null, null, 1000.0f, 2000.0f, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals(1200.0f, results.get(0).getPowerConsumption(), 0.01);
    }

    @Test
    void filterByStatus_returnsExactMatch() {
        var params = new StreetLightFilterParams(
                null, null, null, null, null, LightStatus.OFF, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals(LightStatus.OFF, results.get(0).getStatus());
    }

    @Test
    void filterByTimestampRange_returnsInWindow() {
        var params = new StreetLightFilterParams(
                null, null, null, null, null, null,
                LocalDateTime.of(2026, Month.JUNE, 2, 0, 0, 0),
                LocalDateTime.of(2026, Month.JUNE, 3, 23, 59, 59));
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(2, results.size());
    }

    @Test
    void filterByCombinedFilters_returnsIntersection() {
        var params = new StreetLightFilterParams(
                "CAIRO", 50, null, null, null, LightStatus.ON, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(2, results.size());
        List<String> locations = results.stream().map(StreetLightSensorData::getLocation).toList();
        assertTrue(locations.contains("CAIRO_ZAMALEK"));
        assertTrue(locations.contains("CAIRO_NEW_CAIRO"));
    }

    @Test
    void filterWithNoFilters_returnsAll() {
        var params = new StreetLightFilterParams(
                null, null, null, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(3, results.size());
    }

    @Test
    void filterWithImpossibleRange_returnsEmpty() {
        var params = new StreetLightFilterParams(
                null, 200, null, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(0, results.size());
    }
}
