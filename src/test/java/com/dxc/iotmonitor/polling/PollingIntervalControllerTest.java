package com.dxc.iotmonitor.polling;

import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PollingIntervalControllerTest {

    @Mock
    private PollingIntervalService pollingIntervalService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PollingIntervalController pollingIntervalController;

    private final User user = User.builder()
            .userId(UUID.randomUUID())
            .email("user@example.com")
            .firstName("U")
            .lastName("Ser")
            .password("x")
            .build();

    @BeforeEach
    void setUpSecurityContext() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("user@example.com", null));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getIntervals_returns200WithResponse() {
        PollingIntervalResponse intervalResponse = new PollingIntervalResponse();
        intervalResponse.setTrafficInterval(5);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(pollingIntervalService.getByUser(user)).thenReturn(intervalResponse);

        ResponseEntity<PollingIntervalResponse> response = pollingIntervalController.getIntervals();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(intervalResponse, response.getBody());
        verify(pollingIntervalService).getByUser(user);
    }

    @Test
    void getIntervals_throws_whenUserNotFound() {
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> pollingIntervalController.getIntervals());

        assertEquals("User not found.", ex.getMessage());
        verifyNoInteractions(pollingIntervalService);
    }

    @Test
    void upsert_returns200WithResponse() {
        PollingIntervalRequest request = new PollingIntervalRequest();
        request.setTrafficInterval(5);
        request.setAirPollutionInterval(10);
        request.setStreetLightInterval(15);
        PollingIntervalResponse intervalResponse = new PollingIntervalResponse();
        intervalResponse.setTrafficInterval(5);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(pollingIntervalService.upsert(user, request)).thenReturn(intervalResponse);

        ResponseEntity<PollingIntervalResponse> response = pollingIntervalController.upsert(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(intervalResponse, response.getBody());
        verify(pollingIntervalService).upsert(user, request);
    }

    @Test
    void flush_returns200WithMessage() {
        ResponseEntity<Map<String, String>> response = pollingIntervalController.flush();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Polling intervals flushed successfully.", response.getBody().get("message"));
        verify(pollingIntervalService).flush();
    }
}
