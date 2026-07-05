package com.dxc.iotmonitor.sensor.traffic;

import com.dxc.iotmonitor.enums.CongestionLevel;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.sensor.common.AlertFanOut;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficFilterParams;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorRequest;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorResponse;
import com.dxc.iotmonitor.sensor.traffic.mapper.TrafficSensorMapper;
import com.dxc.iotmonitor.sensor.traffic.model.TrafficSensorData;
import com.dxc.iotmonitor.sensor.traffic.repository.TrafficSensorRepository;
import com.dxc.iotmonitor.sensor.traffic.service.TrafficReadingsExtractor;
import com.dxc.iotmonitor.sensor.traffic.service.TrafficSensorHandler;
import com.dxc.iotmonitor.sensor.traffic.service.TrafficSpecBuilder;
import com.dxc.iotmonitor.sensor.traffic.service.TrafficValidator;
import com.dxc.iotmonitor.user.model.User;
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
import java.util.HashMap;
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
class TrafficSensorHandlerTest {

    @Mock
    private TrafficSensorRepository trafficSensorRepository;

    @Mock
    private TrafficSensorMapper trafficSensorMapper;

    @Mock
    private TrafficValidator trafficValidator;

    @Mock
    private TrafficReadingsExtractor trafficReadingsExtractor;

    @Mock
    private TrafficSpecBuilder trafficSpecBuilder;

    @Mock
    private AlertFanOut alertFanOut;

    @InjectMocks
    private TrafficSensorHandler trafficSensorHandler;

    @Test
    void save_ShouldReturnResponse_WhenRequestIsValid() {
        TrafficSensorRequest request = TrafficSensorRequest.builder()
                .location("CAIRO_RING_ROAD")
                .timestamp(LocalDateTime.now())
                .trafficDensity(200)
                .avgSpeed(60.0f)
                .congestionLevel(CongestionLevel.MODERATE)
                .build();

        TrafficSensorData entity = TrafficSensorData.builder()
                .location("CAIRO_RING_ROAD")
                .timestamp(request.getTimestamp())
                .trafficDensity(200)
                .avgSpeed(60.0f)
                .congestionLevel(CongestionLevel.MODERATE)
                .build();

        UUID responseId = UUID.randomUUID();
        TrafficSensorResponse mappedResponse = TrafficSensorResponse.builder()
                .id(responseId)
                .build();

        Map<Metric, Float> readings = new HashMap<>();
        readings.put(Metric.TRAFFIC_DENSITY, 200.0f);
        readings.put(Metric.AVG_SPEED, 60.0f);

        when(trafficSensorMapper.toEntity(request)).thenReturn(entity);
        when(trafficSensorRepository.save(entity)).thenReturn(entity);
        when(trafficReadingsExtractor.extract(entity)).thenReturn(readings);
        when(trafficSensorMapper.toResponse(entity)).thenReturn(mappedResponse);

        TrafficSensorResponse response = trafficSensorHandler.save(request, Optional.empty());

        assertNotNull(response);
        verify(trafficSensorRepository, times(1)).save(entity);
        verify(alertFanOut, times(1)).fanOut(any(), any(), any(), any(), any());
    }

    @Test
    void save_rejectsAirPollutionLocation() {
        TrafficSensorRequest request = TrafficSensorRequest.builder()
                .location("CAIRO_NASR_CITY")
                .timestamp(LocalDateTime.now())
                .trafficDensity(200)
                .avgSpeed(60.0f)
                .congestionLevel(CongestionLevel.MODERATE)
                .build();

        doThrow(new IllegalArgumentException("invalid location for this sensor type"))
                .when(trafficValidator).validate(request);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> trafficSensorHandler.save(request, Optional.empty()));

        assertEquals("invalid location for this sensor type", ex.getMessage());
        verify(trafficSensorRepository, never()).save(any());
    }

    @Test
    void getFiltered_ShouldReturnPaginatedResults() {
        TrafficFilterParams filters = new TrafficFilterParams(
                "CAIRO", 100, 300, null, null, CongestionLevel.HIGH, null, null
        );
        Pageable pageable = PageRequest.of(0, 10);

        TrafficSensorData mockData = TrafficSensorData.builder()
                .location("CAIRO_RING_ROAD")
                .trafficDensity(200)
                .build();
        Page<TrafficSensorData> mockPage = new PageImpl<>(List.of(mockData));

        Specification<TrafficSensorData> mockSpec = (root, query, cb) -> cb.conjunction();
        when(trafficSpecBuilder.build(any(TrafficFilterParams.class))).thenReturn(mockSpec);
        when(trafficSensorRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        TrafficSensorResponse responseDto = TrafficSensorResponse.builder()
                .id(UUID.randomUUID())
                .build();
        when(trafficSensorMapper.toResponse(any(TrafficSensorData.class))).thenReturn(responseDto);

        Page<TrafficSensorResponse> result = trafficSensorHandler.getFiltered(filters, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(trafficSensorRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void flush_ShouldCallDeleteAll() {
        trafficSensorHandler.flush();

        verify(trafficSensorRepository, times(1)).deleteAll();
    }
}
