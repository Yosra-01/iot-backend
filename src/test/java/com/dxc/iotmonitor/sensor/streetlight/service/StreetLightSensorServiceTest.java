package com.dxc.iotmonitor.sensor.streetlight.service;

import com.dxc.iotmonitor.enums.LightStatus;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorRequest;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorResponse;
import com.dxc.iotmonitor.sensor.streetlight.mapper.StreetLightSensorMapper;
import com.dxc.iotmonitor.sensor.streetlight.model.StreetLightSensorData;
import com.dxc.iotmonitor.sensor.streetlight.repository.StreetLightSensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreetLightSensorServiceTest {

    private static final String LOCATION = "Giza Bridge";
    private static final int BRIGHTNESS_LEVEL = 75;
    private static final float POWER_CONSUMPTION = 1200.0f;

    @Mock
    private StreetLightSensorRepository repository;

    @Mock
    private StreetLightSensorMapper mapper;

    @InjectMocks
    private StreetLightSensorService service;

    private LocalDateTime timestamp;
    private UUID id;
    private StreetLightSensorRequest request;
    private StreetLightSensorData entity;
    private StreetLightSensorResponse response;

    @BeforeEach
    void setUp() {
        timestamp = LocalDateTime.now().minusMinutes(1);
        id = UUID.randomUUID();

        request = StreetLightSensorRequest.builder()
                .location(LOCATION)
                .timestamp(timestamp)
                .brightnessLevel(BRIGHTNESS_LEVEL)
                .powerConsumption(POWER_CONSUMPTION)
                .status(LightStatus.ON)
                .build();

        entity = StreetLightSensorData.builder()
                .id(id)
                .location(LOCATION)
                .timestamp(timestamp)
                .brightnessLevel(BRIGHTNESS_LEVEL)
                .powerConsumption(POWER_CONSUMPTION)
                .status(LightStatus.ON)
                .build();

        response = StreetLightSensorResponse.builder()
                .id(id)
                .location(LOCATION)
                .timestamp(timestamp)
                .brightnessLevel(BRIGHTNESS_LEVEL)
                .powerConsumption(POWER_CONSUMPTION)
                .status(LightStatus.ON)
                .build();
    }

    @Test
    void save_validRequest_returnsSavedResponse() {
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        StreetLightSensorResponse result = service.save(request);

        assertEquals(response, result);
        verify(repository, times(1)).save(entity);
    }

    @Test
    void getAll_returnsListOfResponses() {
        when(repository.findAllByOrderByTimestampDesc()).thenReturn(List.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        List<StreetLightSensorResponse> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals(response, result.getFirst());
    }

    @Test
    void flush_callsDeleteAll() {
        service.flush();

        verify(repository, times(1)).deleteAll();
    }
}
