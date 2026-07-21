package com.dxc.iotmonitor.sensor.airpollution.service;

import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.PollutionLevel;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionFilterParams;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorRequest;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorResponse;
import com.dxc.iotmonitor.sensor.airpollution.mapper.AirPollutionSensorMapper;
import com.dxc.iotmonitor.sensor.airpollution.model.AirPollutionSensorData;
import com.dxc.iotmonitor.sensor.airpollution.repository.AirPollutionSensorRepository;
import com.dxc.iotmonitor.sensor.common.AlertFanOut;
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

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirPollutionSensorHandlerTest {

    @Mock
    private AirPollutionSensorRepository airPollutionSensorRepository;

    @Mock
    private AirPollutionSensorMapper airPollutionSensorMapper;

    @Mock
    private AirPollutionValidator airPollutionValidator;

    @Mock
    private AirPollutionReadingsExtractor airPollutionReadingsExtractor;

    @Mock
    private AirPollutionSpecBuilder airPollutionSpecBuilder;

    @Mock
    private AlertFanOut alertFanOut;

    @InjectMocks
    private AirPollutionSensorHandler airPollutionSensorHandler;

    @Test
    void save_ShouldReturnResponse_WhenRequestIsValid() {
        LocalDateTime timestamp = LocalDateTime.now().minusMinutes(1);
        UUID id = UUID.randomUUID();

        AirPollutionSensorRequest request = AirPollutionSensorRequest.builder()
                .location("CAIRO_NASR_CITY")
                .timestamp(timestamp)
                .pm2_5(120.5f)
                .pm10(200.3f)
                .co(25.1f)
                .no2(30.4f)
                .so2(15.2f)
                .ozone(180.0f)
                .pollutionLevel(PollutionLevel.UNHEALTHY)
                .build();

        AirPollutionSensorData entity = AirPollutionSensorData.builder()
                .id(id)
                .location("CAIRO_NASR_CITY")
                .timestamp(timestamp)
                .pm2_5(120.5f)
                .pm10(200.3f)
                .co(25.1f)
                .no2(30.4f)
                .so2(15.2f)
                .ozone(180.0f)
                .pollutionLevel(PollutionLevel.UNHEALTHY)
                .build();

        AirPollutionSensorResponse response = AirPollutionSensorResponse.builder()
                .id(id)
                .build();

        Map<Metric, Float> readings = new EnumMap<>(Metric.class);
        readings.put(Metric.CO, 25.1f);
        readings.put(Metric.OZONE, 180.0f);

        when(airPollutionSensorMapper.toEntity(request)).thenReturn(entity);
        when(airPollutionSensorRepository.save(entity)).thenReturn(entity);
        when(airPollutionReadingsExtractor.extract(entity)).thenReturn(readings);
        when(airPollutionSensorMapper.toResponse(entity)).thenReturn(response);

        AirPollutionSensorResponse result = airPollutionSensorHandler.save(request, Optional.empty());

        assertNotNull(result);
        verify(airPollutionSensorRepository, times(1)).save(entity);
        verify(alertFanOut, times(1)).fanOut(any(), any(), any(), any(), any());
    }

    @Test
    void save_rejectsTrafficLocation() {
        AirPollutionSensorRequest request = AirPollutionSensorRequest.builder()
                .location("CAIRO_RING_ROAD")
                .timestamp(LocalDateTime.now().minusMinutes(1))
                .pm2_5(120.5f)
                .pm10(200.3f)
                .co(25.1f)
                .no2(30.4f)
                .so2(15.2f)
                .ozone(180.0f)
                .pollutionLevel(PollutionLevel.UNHEALTHY)
                .build();

        doThrow(new IllegalArgumentException("invalid location for this sensor type"))
                .when(airPollutionValidator).validate(request);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> airPollutionSensorHandler.save(request, Optional.empty()));

        assertEquals("invalid location for this sensor type", ex.getMessage());
        verify(airPollutionSensorRepository, never()).save(any());
    }

    @Test
    void getFiltered_ShouldReturnPaginatedResults() {
        AirPollutionFilterParams filters = new AirPollutionFilterParams(
                "CAIRO", null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null
        );
        Pageable pageable = PageRequest.of(0, 10);

        AirPollutionSensorData mockData = AirPollutionSensorData.builder()
                .location("CAIRO_NASR_CITY")
                .build();
        Page<AirPollutionSensorData> mockPage = new PageImpl<>(List.of(mockData));

        Specification<AirPollutionSensorData> mockSpec = (root, query, cb) -> cb.conjunction();
        when(airPollutionSpecBuilder.build(any(AirPollutionFilterParams.class))).thenReturn(mockSpec);
        when(airPollutionSensorRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        AirPollutionSensorResponse responseDto = AirPollutionSensorResponse.builder()
                .id(UUID.randomUUID())
                .build();
        when(airPollutionSensorMapper.toResponse(any(AirPollutionSensorData.class))).thenReturn(responseDto);

        Page<AirPollutionSensorResponse> result = airPollutionSensorHandler.getFiltered(filters, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(airPollutionSensorRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void flush_ShouldCallDeleteAll() {
        airPollutionSensorHandler.flush();

        verify(airPollutionSensorRepository, times(1)).deleteAll();
    }
}
