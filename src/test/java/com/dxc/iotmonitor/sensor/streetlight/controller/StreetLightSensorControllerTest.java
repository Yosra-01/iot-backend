package com.dxc.iotmonitor.sensor.streetlight.controller;

import com.dxc.iotmonitor.enums.LightStatus;
import com.dxc.iotmonitor.sensor.common.AuthenticatedUserResolver;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorRequest;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorResponse;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightStatsResponse;
import com.dxc.iotmonitor.sensor.streetlight.service.StreetLightSensorHandler;
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
class StreetLightSensorControllerTest {

    @Mock
    private StreetLightSensorHandler streetLightSensorHandler;

    @Mock
    private AuthenticatedUserResolver authenticatedUserResolver;

    @InjectMocks
    private StreetLightSensorController streetLightSensorController;

    private final User user = User.builder()
            .userId(UUID.randomUUID())
            .email("u@example.com")
            .firstName("U")
            .lastName("Ser")
            .password("x")
            .build();

    @Test
    void create_returns201WithResponse() {
        StreetLightSensorRequest request = new StreetLightSensorRequest();
        StreetLightSensorResponse sensorResponse = new StreetLightSensorResponse();
        sensorResponse.setId(UUID.randomUUID());
        when(authenticatedUserResolver.current()).thenReturn(Optional.of(user));
        when(streetLightSensorHandler.save(request, Optional.of(user))).thenReturn(sensorResponse);

        ResponseEntity<StreetLightSensorResponse> response = streetLightSensorController.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(sensorResponse, response.getBody());
        verify(streetLightSensorHandler).save(request, Optional.of(user));
    }

    @Test
    void listAll_returns200WithPage() {
        StreetLightSensorResponse sensorResponse = new StreetLightSensorResponse();
        Page<StreetLightSensorResponse> page = new PageImpl<>(List.of(sensorResponse));
        when(streetLightSensorHandler.getFiltered(any(), any())).thenReturn(page);

        ResponseEntity<Page<StreetLightSensorResponse>> response = streetLightSensorController.listAll(
                "CAIRO_ZAMALEK", 10, 80, 50f, 500f, LightStatus.ON,
                LocalDateTime.now().minusDays(1), LocalDateTime.now(),
                0, 20, "timestamp", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        var content = response.getBody().getContent();
        assertEquals(1, content.size());
        verify(streetLightSensorHandler).getFiltered(any(), any());
    }

    @Test
    void stats_returns200WithStatsResponse() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();
        StreetLightStatsResponse statsResponse = new StreetLightStatsResponse();
        when(streetLightSensorHandler.getStats(from, to, "CAIRO_ZAMALEK")).thenReturn(statsResponse);

        ResponseEntity<StreetLightStatsResponse> response =
                streetLightSensorController.stats(from, to, "CAIRO_ZAMALEK");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(statsResponse, response.getBody());
        verify(streetLightSensorHandler).getStats(from, to, "CAIRO_ZAMALEK");
    }

    @Test
    void getLatest_returns200WithResponse() {
        StreetLightSensorResponse sensorResponse = new StreetLightSensorResponse();
        when(streetLightSensorHandler.getLatest()).thenReturn(sensorResponse);

        ResponseEntity<StreetLightSensorResponse> response = streetLightSensorController.getLatest();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sensorResponse, response.getBody());
        verify(streetLightSensorHandler).getLatest();
    }

    @Test
    void getById_returns200WithResponse() {
        String id = UUID.randomUUID().toString();
        StreetLightSensorResponse sensorResponse = new StreetLightSensorResponse();
        when(streetLightSensorHandler.getById(id)).thenReturn(sensorResponse);

        ResponseEntity<StreetLightSensorResponse> response = streetLightSensorController.getById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sensorResponse, response.getBody());
        verify(streetLightSensorHandler).getById(id);
    }

    @Test
    void flush_returns200WithMessage() {
        ResponseEntity<String> response = streetLightSensorController.flush();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Street light sensor data flushed successfully.", response.getBody());
        verify(streetLightSensorHandler).flush();
    }
}
