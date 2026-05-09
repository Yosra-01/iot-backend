package com.dxc.iotmonitor.sensor.traffic;

import com.dxc.iotmonitor.enums.*;
import com.dxc.iotmonitor.sensor.traffic.dto.*;
import com.dxc.iotmonitor.sensor.traffic.mapper.*;
import com.dxc.iotmonitor.sensor.traffic.model.*;
import com.dxc.iotmonitor.sensor.traffic.repository.*;
import com.dxc.iotmonitor.sensor.traffic.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrafficSensorServiceTest {

    @Mock
    private TrafficSensorRepository trafficSensorRepository;

    @Mock
    private TrafficSensorMapper trafficSensorMapper;

    @InjectMocks
    private TrafficSensorService trafficSensorService;

    @Test
    void save_ShouldReturnResponse_WhenRequestIsValid() {
        TrafficSensorRequest request = TrafficSensorRequest.builder()
                .location("Cairo")
                .timestamp(LocalDateTime.now())
                .trafficDensity(200)
                .avgSpeed(60.0f)
                .congestionLevel(CongestionLevel.MODERATE)
                .build();

        TrafficSensorData entity = TrafficSensorData.builder()
                .location("Cairo")
                .timestamp(request.getTimestamp())
                .trafficDensity(200)
                .avgSpeed(60.0f)
                .congestionLevel(CongestionLevel.MODERATE)
                .build();

        UUID responseId = UUID.randomUUID();
        TrafficSensorResponse mappedResponse = TrafficSensorResponse.builder()
                .id(responseId)
                .build();

        when(trafficSensorMapper.toEntity(request)).thenReturn(entity);
        when(trafficSensorRepository.save(entity)).thenReturn(entity);
        when(trafficSensorMapper.toResponse(entity)).thenReturn(mappedResponse);

        TrafficSensorResponse response = trafficSensorService.save(request);

        assertNotNull(response);
        verify(trafficSensorRepository, times(1)).save(entity);
    }

    @Test
    void getAll_ShouldReturnList_WhenDataExists() {
        TrafficSensorData entity = TrafficSensorData.builder()
                .location("Cairo")
                .timestamp(LocalDateTime.now())
                .trafficDensity(100)
                .avgSpeed(50.0f)
                .congestionLevel(CongestionLevel.LOW)
                .build();

        TrafficSensorResponse responseDto = TrafficSensorResponse.builder()
                .id(UUID.randomUUID())
                .build();

        when(trafficSensorRepository.findAllByOrderByTimestampDesc()).thenReturn(List.of(entity));
        when(trafficSensorMapper.toResponse(any(TrafficSensorData.class))).thenReturn(responseDto);

        List<TrafficSensorResponse> result = trafficSensorService.getAll();

        assertEquals(1, result.size());
    }

    @Test
    void flush_ShouldCallDeleteAll() {
        trafficSensorService.flush();

        verify(trafficSensorRepository, times(1)).deleteAll();
    }
}
