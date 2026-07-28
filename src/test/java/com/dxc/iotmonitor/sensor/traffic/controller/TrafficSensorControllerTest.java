package com.dxc.iotmonitor.sensor.traffic.controller;

import com.dxc.iotmonitor.enums.CongestionLevel;
import com.dxc.iotmonitor.sensor.common.AuthenticatedUserResolver;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorRequest;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorResponse;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficStatsResponse;
import com.dxc.iotmonitor.sensor.traffic.service.TrafficSensorHandler;
import com.dxc.iotmonitor.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrafficSensorControllerTest {

    @Mock
    private TrafficSensorHandler trafficSensorHandler;

    @Mock
    private AuthenticatedUserResolver authenticatedUserResolver;

    @InjectMocks
    private TrafficSensorController trafficSensorController;

    private final User user = User.builder()
            .userId(UUID.randomUUID())
            .email("u@example.com")
            .firstName("U")
            .lastName("Ser")
            .password("x")
            .build();

    @Test
    void create_returns201WithResponse() {
        TrafficSensorRequest request = new TrafficSensorRequest();
        TrafficSensorResponse sensorResponse = new TrafficSensorResponse();
        sensorResponse.setId(UUID.randomUUID());
        when(authenticatedUserResolver.current()).thenReturn(Optional.of(user));
        when(trafficSensorHandler.save(request, Optional.of(user))).thenReturn(sensorResponse);

        ResponseEntity<TrafficSensorResponse> response = trafficSensorController.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(sensorResponse, response.getBody());
        verify(trafficSensorHandler).save(request, Optional.of(user));
    }

    @Test
    void listAll_returns200WithPage() {
        TrafficSensorResponse sensorResponse = new TrafficSensorResponse();
        Page<TrafficSensorResponse> page = new PageImpl<>(List.of(sensorResponse));
        when(trafficSensorHandler.getFiltered(any(), any())).thenReturn(page);

        ResponseEntity<Page<TrafficSensorResponse>> response = trafficSensorController.listAll(
                "CAIRO_RING_ROAD", 10, 100, 20f, 80f, CongestionLevel.LOW,
                LocalDateTime.now().minusDays(1), LocalDateTime.now(),
                0, 20, "timestamp", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        var content = response.getBody().getContent();
        assertEquals(1, content.size());
        verify(trafficSensorHandler).getFiltered(any(), any());
    }

    @Test
    void stats_returns200WithStatsResponse() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();
        TrafficStatsResponse statsResponse = new TrafficStatsResponse();
        when(trafficSensorHandler.getStats(from, to, "CAIRO_RING_ROAD")).thenReturn(statsResponse);

        ResponseEntity<TrafficStatsResponse> response = trafficSensorController.stats(from, to, "CAIRO_RING_ROAD");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(statsResponse, response.getBody());
        verify(trafficSensorHandler).getStats(from, to, "CAIRO_RING_ROAD");
    }

    @Test
    void getLatest_returns200WithResponse() {
        TrafficSensorResponse sensorResponse = new TrafficSensorResponse();
        when(trafficSensorHandler.getLatest()).thenReturn(sensorResponse);

        ResponseEntity<TrafficSensorResponse> response = trafficSensorController.getLatest();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sensorResponse, response.getBody());
        verify(trafficSensorHandler).getLatest();
    }

    @Test
    void getById_returns200WithResponse() {
        String id = UUID.randomUUID().toString();
        TrafficSensorResponse sensorResponse = new TrafficSensorResponse();
        when(trafficSensorHandler.getById(id)).thenReturn(sensorResponse);

        ResponseEntity<TrafficSensorResponse> response = trafficSensorController.getById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sensorResponse, response.getBody());
        verify(trafficSensorHandler).getById(id);
    }

    @Test
    void flush_returns200WithMessage() {
        ResponseEntity<String> response = trafficSensorController.flush();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Traffic sensor data flushed successfully.", response.getBody());
        verify(trafficSensorHandler).flush();
    }
}
