package com.dxc.iotmonitor.sensor.airpollution.controller;

import com.dxc.iotmonitor.enums.PollutionLevel;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorRequest;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorResponse;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionStatsResponse;
import com.dxc.iotmonitor.sensor.airpollution.service.AirPollutionSensorHandler;
import com.dxc.iotmonitor.sensor.common.AuthenticatedUserResolver;
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
class AirPollutionSensorControllerTest {

    @Mock
    private AirPollutionSensorHandler airPollutionSensorHandler;

    @Mock
    private AuthenticatedUserResolver authenticatedUserResolver;

    @InjectMocks
    private AirPollutionSensorController airPollutionSensorController;

    private final User user = User.builder()
            .userId(UUID.randomUUID())
            .email("u@example.com")
            .firstName("U")
            .lastName("Ser")
            .password("x")
            .build();

    @Test
    void create_returns201WithResponse() {
        AirPollutionSensorRequest request = new AirPollutionSensorRequest();
        AirPollutionSensorResponse sensorResponse = new AirPollutionSensorResponse();
        sensorResponse.setId(UUID.randomUUID());
        when(authenticatedUserResolver.current()).thenReturn(Optional.of(user));
        when(airPollutionSensorHandler.save(request, Optional.of(user))).thenReturn(sensorResponse);

        ResponseEntity<AirPollutionSensorResponse> response = airPollutionSensorController.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(sensorResponse, response.getBody());
        verify(airPollutionSensorHandler).save(request, Optional.of(user));
    }

    @Test
    void listAll_returns200WithPage() {
        AirPollutionSensorResponse sensorResponse = new AirPollutionSensorResponse();
        Page<AirPollutionSensorResponse> page = new PageImpl<>(List.of(sensorResponse));
        when(airPollutionSensorHandler.getFiltered(any(), any())).thenReturn(page);

        ResponseEntity<Page<AirPollutionSensorResponse>> response = airPollutionSensorController.listAll(
                "CAIRO_NASR_CITY", 10f, 50f, 20f, 100f, 1f, 10f, 5f, 20f, 5f, 15f, 10f, 30f,
                PollutionLevel.GOOD, LocalDateTime.now().minusDays(1), LocalDateTime.now(),
                0, 20, "timestamp", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        var content = response.getBody().getContent();
        assertEquals(1, content.size());
        verify(airPollutionSensorHandler).getFiltered(any(), any());
    }

    @Test
    void stats_returns200WithStatsResponse() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();
        AirPollutionStatsResponse statsResponse = new AirPollutionStatsResponse();
        when(airPollutionSensorHandler.getStats(from, to, "CAIRO_NASR_CITY")).thenReturn(statsResponse);

        ResponseEntity<AirPollutionStatsResponse> response =
                airPollutionSensorController.stats(from, to, "CAIRO_NASR_CITY");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(statsResponse, response.getBody());
        verify(airPollutionSensorHandler).getStats(from, to, "CAIRO_NASR_CITY");
    }

    @Test
    void getLatest_returns200WithResponse() {
        AirPollutionSensorResponse sensorResponse = new AirPollutionSensorResponse();
        when(airPollutionSensorHandler.getLatest()).thenReturn(sensorResponse);

        ResponseEntity<AirPollutionSensorResponse> response = airPollutionSensorController.getLatest();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sensorResponse, response.getBody());
        verify(airPollutionSensorHandler).getLatest();
    }

    @Test
    void getById_returns200WithResponse() {
        String id = UUID.randomUUID().toString();
        AirPollutionSensorResponse sensorResponse = new AirPollutionSensorResponse();
        when(airPollutionSensorHandler.getById(id)).thenReturn(sensorResponse);

        ResponseEntity<AirPollutionSensorResponse> response = airPollutionSensorController.getById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sensorResponse, response.getBody());
        verify(airPollutionSensorHandler).getById(id);
    }

    @Test
    void flush_returns200WithMessage() {
        ResponseEntity<String> response = airPollutionSensorController.flush();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Air pollution sensor data flushed successfully.", response.getBody());
        verify(airPollutionSensorHandler).flush();
    }
}
