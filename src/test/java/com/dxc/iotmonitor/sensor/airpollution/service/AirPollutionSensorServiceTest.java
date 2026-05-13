package com.dxc.iotmonitor.sensor.airpollution.service;

import com.dxc.iotmonitor.alert.service.AlertService;
import com.dxc.iotmonitor.enums.PollutionLevel;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorRequest;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorResponse;
import com.dxc.iotmonitor.sensor.airpollution.mapper.AirPollutionSensorMapper;
import com.dxc.iotmonitor.sensor.airpollution.model.AirPollutionSensorData;
import com.dxc.iotmonitor.sensor.airpollution.repository.AirPollutionSensorRepository;
import com.dxc.iotmonitor.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirPollutionSensorServiceTest {

    private static final String LOCATION = "CAIRO_NASR_CITY";
    private static final float PM2_5 = 120.5f;
    private static final float PM10 = 200.3f;
    private static final float CO = 25.1f;
    private static final float NO2 = 30.4f;
    private static final float SO2 = 15.2f;
    private static final float OZONE = 180.0f;

    @Mock
    private AirPollutionSensorRepository repository;

    @Mock
    private AirPollutionSensorMapper mapper;

    @Mock
    private AlertService alertService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AirPollutionSensorService service;

    private LocalDateTime timestamp;
    private UUID id;
    private AirPollutionSensorRequest request;
    private AirPollutionSensorData entity;
    private AirPollutionSensorResponse response;

    @BeforeEach
    void setUp() {
        timestamp = LocalDateTime.now().minusMinutes(1);
        id = UUID.randomUUID();

        request = AirPollutionSensorRequest.builder()
                .location(LOCATION)
                .timestamp(timestamp)
                .pm2_5(PM2_5)
                .pm10(PM10)
                .co(CO)
                .no2(NO2)
                .so2(SO2)
                .ozone(OZONE)
                .pollutionLevel(PollutionLevel.UNHEALTHY)
                .build();

        entity = AirPollutionSensorData.builder()
                .id(id)
                .location(LOCATION)
                .timestamp(timestamp)
                .pm2_5(PM2_5)
                .pm10(PM10)
                .co(CO)
                .no2(NO2)
                .so2(SO2)
                .ozone(OZONE)
                .pollutionLevel(PollutionLevel.UNHEALTHY)
                .build();

        response = AirPollutionSensorResponse.builder()
                .id(id)
                .location(LOCATION)
                .timestamp(timestamp)
                .pm2_5(PM2_5)
                .pm10(PM10)
                .co(CO)
                .no2(NO2)
                .so2(SO2)
                .ozone(OZONE)
                .pollutionLevel(PollutionLevel.UNHEALTHY)
                .build();
    }

    @Test
    void save_validRequest_returnsSavedResponse() {
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        AirPollutionSensorResponse result = service.save(request, Optional.empty());

        assertEquals(response, result);
        verify(repository, times(1)).save(entity);
    }

    @Test
    void getAll_returnsListOfResponses() {
        when(repository.findAllByOrderByTimestampDesc()).thenReturn(List.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        List<AirPollutionSensorResponse> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals(response, result.getFirst());
    }

    @Test
    void flush_callsDeleteAll() {
        service.flush();

        verify(repository, times(1)).deleteAll();
    }
}
