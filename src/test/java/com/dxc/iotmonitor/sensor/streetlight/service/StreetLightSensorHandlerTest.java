package com.dxc.iotmonitor.sensor.streetlight.service;

import com.dxc.iotmonitor.enums.LightStatus;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.sensor.common.AlertFanOut;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightFilterParams;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorRequest;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorResponse;
import com.dxc.iotmonitor.sensor.streetlight.mapper.StreetLightSensorMapper;
import com.dxc.iotmonitor.sensor.streetlight.model.StreetLightSensorData;
import com.dxc.iotmonitor.sensor.streetlight.repository.StreetLightSensorRepository;
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
class StreetLightSensorHandlerTest {

    @Mock
    private StreetLightSensorRepository streetLightSensorRepository;

    @Mock
    private StreetLightSensorMapper streetLightSensorMapper;

    @Mock
    private StreetLightValidator streetLightValidator;

    @Mock
    private StreetLightReadingsExtractor streetLightReadingsExtractor;

    @Mock
    private StreetLightSpecBuilder streetLightSpecBuilder;

    @Mock
    private AlertFanOut alertFanOut;

    @InjectMocks
    private StreetLightSensorHandler streetLightSensorHandler;

    @Test
    void save_ShouldReturnResponse_WhenRequestIsValid() {
        LocalDateTime timestamp = LocalDateTime.now().minusMinutes(1);
        UUID id = UUID.randomUUID();

        StreetLightSensorRequest request = StreetLightSensorRequest.builder()
                .location("CAIRO_ZAMALEK")
                .timestamp(timestamp)
                .brightnessLevel(75)
                .powerConsumption(1200.0f)
                .status(LightStatus.ON)
                .build();

        StreetLightSensorData entity = StreetLightSensorData.builder()
                .id(id)
                .location("CAIRO_ZAMALEK")
                .timestamp(timestamp)
                .brightnessLevel(75)
                .powerConsumption(1200.0f)
                .status(LightStatus.ON)
                .build();

        StreetLightSensorResponse response = StreetLightSensorResponse.builder()
                .id(id)
                .build();

        Map<Metric, Float> readings = new HashMap<>();
        readings.put(Metric.BRIGHTNESS_LEVEL, 75.0f);
        readings.put(Metric.POWER_CONSUMPTION, 1200.0f);

        when(streetLightSensorMapper.toEntity(request)).thenReturn(entity);
        when(streetLightSensorRepository.save(entity)).thenReturn(entity);
        when(streetLightReadingsExtractor.extract(entity)).thenReturn(readings);
        when(streetLightSensorMapper.toResponse(entity)).thenReturn(response);

        StreetLightSensorResponse result = streetLightSensorHandler.save(request, Optional.empty());

        assertNotNull(result);
        verify(streetLightSensorRepository, times(1)).save(entity);
        verify(alertFanOut, times(1)).fanOut(any(), any(), any(), any(), any());
    }

    @Test
    void save_rejectsTrafficLocation() {
        StreetLightSensorRequest request = StreetLightSensorRequest.builder()
                .location("CAIRO_RING_ROAD")
                .timestamp(LocalDateTime.now().minusMinutes(1))
                .brightnessLevel(75)
                .powerConsumption(1200.0f)
                .status(LightStatus.ON)
                .build();

        doThrow(new IllegalArgumentException("invalid location for this sensor type"))
                .when(streetLightValidator).validate(request);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> streetLightSensorHandler.save(request, Optional.empty()));

        assertEquals("invalid location for this sensor type", ex.getMessage());
        verify(streetLightSensorRepository, never()).save(any());
    }

    @Test
    void getFiltered_ShouldReturnPaginatedResults() {
        StreetLightFilterParams filters = new StreetLightFilterParams(
                "CAIRO", null, null, null, null, null, null, null
        );
        Pageable pageable = PageRequest.of(0, 10);

        StreetLightSensorData mockData = StreetLightSensorData.builder()
                .location("CAIRO_ZAMALEK")
                .build();
        Page<StreetLightSensorData> mockPage = new PageImpl<>(List.of(mockData));

        Specification<StreetLightSensorData> mockSpec = (root, query, cb) -> cb.conjunction();
        when(streetLightSpecBuilder.build(any(StreetLightFilterParams.class))).thenReturn(mockSpec);
        when(streetLightSensorRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        StreetLightSensorResponse responseDto = StreetLightSensorResponse.builder()
                .id(UUID.randomUUID())
                .build();
        when(streetLightSensorMapper.toResponse(any(StreetLightSensorData.class))).thenReturn(responseDto);

        Page<StreetLightSensorResponse> result = streetLightSensorHandler.getFiltered(filters, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(streetLightSensorRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void flush_ShouldCallDeleteAll() {
        streetLightSensorHandler.flush();

        verify(streetLightSensorRepository, times(1)).deleteAll();
    }
}
