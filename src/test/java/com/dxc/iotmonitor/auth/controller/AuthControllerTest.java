package com.dxc.iotmonitor.auth.controller;

import com.dxc.iotmonitor.auth.dto.AuthResponse;
import com.dxc.iotmonitor.auth.dto.LoginRequest;
import com.dxc.iotmonitor.auth.dto.SignupRequest;
import com.dxc.iotmonitor.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void createUser_returns201WithAuthResponse() {
        SignupRequest request = new SignupRequest(
                "john.doe@example.com", "John", "Doe", "SecurePass1!");
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken("mocked-jwt-token");
        authResponse.setMessage("User registered successfully.");
        when(authService.createUser(request)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.createUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(authResponse, response.getBody());
        verify(authService).createUser(request);
    }

    @Test
    void login_returns200WithAuthResponse() {
        LoginRequest request = new LoginRequest("john.doe@example.com", "SecurePass1!");
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken("mocked-jwt-token");
        authResponse.setMessage("Login successful.");
        when(authService.login(request)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(authResponse, response.getBody());
        verify(authService).login(request);
    }

    @Test
    void logout_returns204_whenBearerTokenProvided() {
        String authHeader = "Bearer mocked-jwt-token";

        ResponseEntity<Void> response = authController.logout(authHeader);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(authService).logout("mocked-jwt-token");
    }

    @Test
    void logout_returns400_whenAuthorizationHeaderIsNull() {
        ResponseEntity<Void> response = authController.logout(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(authService);
    }

    @Test
    void logout_returns400_whenAuthorizationHeaderDoesNotStartWithBearer() {
        ResponseEntity<Void> response = authController.logout("Basic abc123");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(authService);
    }
}
