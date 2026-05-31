package com.dxc.iotmonitor.sensor.traffic;

import com.dxc.iotmonitor.alert.service.AlertService;
import com.dxc.iotmonitor.enums.CongestionLevel;
import com.dxc.iotmonitor.sensor.traffic.dto.*;
import com.dxc.iotmonitor.sensor.traffic.mapper.*;
import com.dxc.iotmonitor.sensor.traffic.model.*;
import com.dxc.iotmonitor.sensor.traffic.repository.*;
import com.dxc.iotmonitor.sensor.traffic.service.*;
import com.dxc.iotmonitor.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrafficSensorServiceTest {

    @Mock
    private TrafficSensorRepository trafficSensorRepository;

    @Mock
    private TrafficSensorMapper trafficSensorMapper;

    @Mock
    private AlertService alertService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TrafficSensorService trafficSensorService;

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

        when(trafficSensorMapper.toEntity(request)).thenReturn(entity);
        when(trafficSensorRepository.save(entity)).thenReturn(entity);
        when(trafficSensorMapper.toResponse(entity)).thenReturn(mappedResponse);
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        TrafficSensorResponse response = trafficSensorService.save(request, Optional.empty());

        assertNotNull(response);
        verify(trafficSensorRepository, times(1)).save(entity);
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

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> trafficSensorService.save(request, Optional.empty()));

        assertEquals("invalid location for this sensor type", ex.getMessage());
        verify(trafficSensorRepository, never()).save(any());
    }

    // THE NEW TEST REPLACING getAll()
    @Test
    void getFilteredTrafficData_ShouldReturnPaginatedResults() {
        // Arrange
        TrafficSensorData mockData = TrafficSensorData.builder()
                .location("CAIRO_RING_ROAD")
                .trafficDensity(200)
                .build();
        Page<TrafficSensorData> mockPage = new PageImpl<>(List.of(mockData));
        Pageable pageable = PageRequest.of(0, 10);

        // Tell Mockito to return our mockPage when the repository is called with ANY Specification
        when(trafficSensorRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        TrafficSensorResponse responseDto = TrafficSensorResponse.builder()
                .id(UUID.randomUUID())
                .build();
        when(trafficSensorMapper.toResponse(any(TrafficSensorData.class))).thenReturn(responseDto);

        // Act
        Page<TrafficSensorResponse> result = trafficSensorService.getFilteredTrafficData(
                "CAIRO", 100, 300, null, null, CongestionLevel.HIGH, null, null, pageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(trafficSensorRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void flush_ShouldCallDeleteAll() {
        trafficSensorService.flush();

        verify(trafficSensorRepository, times(1)).deleteAll();
    }
}