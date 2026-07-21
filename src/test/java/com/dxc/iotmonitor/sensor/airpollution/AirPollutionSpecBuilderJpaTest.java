package com.dxc.iotmonitor.sensor.airpollution;

import com.dxc.iotmonitor.enums.PollutionLevel;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionFilterParams;
import com.dxc.iotmonitor.sensor.airpollution.model.AirPollutionSensorData;
import com.dxc.iotmonitor.sensor.airpollution.repository.AirPollutionSensorRepository;
import com.dxc.iotmonitor.sensor.airpollution.service.AirPollutionSpecBuilder;
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
class AirPollutionSpecBuilderJpaTest {

    @Autowired
    private AirPollutionSensorRepository repository;

    private final AirPollutionSpecBuilder specBuilder = new AirPollutionSpecBuilder();

    private AirPollutionSensorData row1, row2, row3;

    @BeforeEach
    void setUp() {
        row1 = AirPollutionSensorData.builder()
                .location("CAIRO_NASR_CITY")
                .pm2_5(120.5f)
                .pm10(200.0f)
                .co(25.0f)
                .no2(30.0f)
                .so2(15.0f)
                .ozone(180.0f)
                .pollutionLevel(PollutionLevel.UNHEALTHY)
                .timestamp(LocalDateTime.of(2026, Month.JUNE, 1, 10, 0, 0))
                .build();

        row2 = AirPollutionSensorData.builder()
                .location("CAIRO_MAADI")
                .pm2_5(50.0f)
                .pm10(60.0f)
                .co(10.0f)
                .no2(15.0f)
                .so2(5.0f)
                .ozone(60.0f)
                .pollutionLevel(PollutionLevel.GOOD)
                .timestamp(LocalDateTime.of(2026, Month.JUNE, 2, 10, 0, 0))
                .build();

        row3 = AirPollutionSensorData.builder()
                .location("CAIRO_HELIOPOLIS")
                .pm2_5(300.0f)
                .pm10(400.0f)
                .co(45.0f)
                .no2(150.0f)
                .so2(250.0f)
                .ozone(250.0f)
                .pollutionLevel(PollutionLevel.HAZARDOUS)
                .timestamp(LocalDateTime.of(2026, Month.JUNE, 3, 10, 0, 0))
                .build();

        repository.saveAll(List.of(row1, row2, row3));
    }

    @Test
    void filterByLocation_returnsPartialMatch() {
        var params = new AirPollutionFilterParams(
                "MAADI", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals("CAIRO_MAADI", results.get(0).getLocation());
    }

    @Test
    void filterByPm2_5Range_returnsInRange() {
        var params = new AirPollutionFilterParams(
                null, 100.0f, 200.0f, null, null, null, null,
                null, null, null, null, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals(120.5f, results.get(0).getPm2_5(), 0.01);
    }

    @Test
    void filterByCoRange_returnsInRange() {
        var params = new AirPollutionFilterParams(
                null, null, null, null, null, 20.0f, 30.0f,
                null, null, null, null, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals(25.0f, results.get(0).getCo(), 0.01);
    }

    @Test
    void filterByOzoneRange_returnsInRange() {
        var params = new AirPollutionFilterParams(
                null, null, null, null, null, null, null,
                null, null, null, null, 150.0f, 200.0f, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals(180.0f, results.get(0).getOzone(), 0.01);
    }

    @Test
    void filterByPollutionLevel_returnsExactMatch() {
        var params = new AirPollutionFilterParams(
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, PollutionLevel.GOOD, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals(PollutionLevel.GOOD, results.get(0).getPollutionLevel());
    }

    @Test
    void filterByTimestampRange_returnsInWindow() {
        var params = new AirPollutionFilterParams(
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                LocalDateTime.of(2026, Month.JUNE, 2, 0, 0, 0),
                LocalDateTime.of(2026, Month.JUNE, 3, 23, 59, 59));
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(2, results.size());
    }

    @Test
    void filterByCombinedFilters_returnsIntersection() {
        var params = new AirPollutionFilterParams(
                "CAIRO", null, null, null, null, 20.0f, null,
                null, null, null, null, null, null, PollutionLevel.UNHEALTHY, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(1, results.size());
        assertEquals("CAIRO_NASR_CITY", results.get(0).getLocation());
        assertEquals(PollutionLevel.UNHEALTHY, results.get(0).getPollutionLevel());
    }

    @Test
    void filterWithNoFilters_returnsAll() {
        var params = new AirPollutionFilterParams(
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(3, results.size());
    }

    @Test
    void filterWithImpossibleRange_returnsEmpty() {
        var params = new AirPollutionFilterParams(
                null, null, null, null, null, 100.0f, null,
                null, null, null, null, null, null, null, null, null);
        var results = repository.findAll(specBuilder.build(params));

        assertEquals(0, results.size());
    }
}
