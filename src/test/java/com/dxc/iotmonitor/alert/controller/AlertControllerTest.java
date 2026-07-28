package com.dxc.iotmonitor.alert.controller;

import com.dxc.iotmonitor.alert.dto.response.AlertResponse;
import com.dxc.iotmonitor.alert.service.AlertService;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertControllerTest {

    @Mock
    private AlertService alertService;

    @Mock
    private AuthenticatedUserResolver authenticatedUserResolver;

    @InjectMocks
    private AlertController alertController;

    private final User user = User.builder()
            .userId(UUID.randomUUID())
            .email("u@example.com")
            .firstName("U")
            .lastName("Ser")
            .password("x")
            .build();

    @Test
    void findAll_returns200WithPage() {
        AlertResponse alertResponse = new AlertResponse();
        alertResponse.setId(UUID.randomUUID());
        Page<AlertResponse> page = new PageImpl<>(List.of(alertResponse));
        when(authenticatedUserResolver.current()).thenReturn(Optional.of(user));
        when(alertService.findFiltered(any(), any(), eq(user))).thenReturn(page);

        ResponseEntity<Page<AlertResponse>> response = alertController.findAll(
                SensorType.TRAFFIC, null, null, null, null, null, null,
                null, null, null, 0, 20, "triggeredAt", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        var content = response.getBody().getContent();
        assertEquals(1, content.size());
        verify(alertService).findFiltered(any(), any(), eq(user));
    }

    @Test
    void findAll_throws_whenUserNotFound() {
        when(authenticatedUserResolver.current()).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> alertController.findAll(
                        null, null, null, null, null, null, null,
                        null, null, null, 0, 20, "triggeredAt", "desc"));

        assertEquals("User not found.", ex.getMessage());
        verifyNoInteractions(alertService);
    }

    @Test
    void count_returns200WithCount() {
        when(authenticatedUserResolver.current()).thenReturn(Optional.of(user));
        when(alertService.count(any(), eq(user))).thenReturn(5L);

        ResponseEntity<Map<String, Long>> response = alertController.count(
                SensorType.TRAFFIC, null, null, null, null, null, Boolean.FALSE,
                null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5L, response.getBody().get("count"));
        verify(alertService).count(any(), eq(user));
    }

    @Test
    void findById_returns200WithAlertResponse() {
        UUID alertId = UUID.randomUUID();
        AlertResponse alertResponse = new AlertResponse();
        alertResponse.setId(alertId);
        when(authenticatedUserResolver.current()).thenReturn(Optional.of(user));
        when(alertService.findById(alertId, user)).thenReturn(alertResponse);

        ResponseEntity<AlertResponse> response = alertController.findById(alertId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(alertResponse, response.getBody());
        verify(alertService).findById(alertId, user);
    }

    @Test
    void markAsRead_returns200WithMessage() {
        UUID alertId = UUID.randomUUID();
        when(authenticatedUserResolver.current()).thenReturn(Optional.of(user));

        ResponseEntity<Map<String, String>> response = alertController.markAsRead(alertId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Alert marked as read.", response.getBody().get("message"));
        verify(alertService).markAsRead(alertId, user);
    }

    @Test
    void flush_returns200WithMessage() {
        ResponseEntity<Map<String, String>> response = alertController.flush();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Alerts flushed successfully.", response.getBody().get("message"));
        verify(alertService).flush();
    }

    @Test
    void deleteById_returns200WithMessage() {
        UUID alertId = UUID.randomUUID();
        when(authenticatedUserResolver.current()).thenReturn(Optional.of(user));

        ResponseEntity<Map<String, String>> response = alertController.deleteById(alertId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Alert dismissed successfully.", response.getBody().get("message"));
        verify(alertService).deleteById(alertId, user);
    }
}
