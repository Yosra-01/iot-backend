package com.dxc.iotmonitor.exception;

import org.junit.jupiter.api.Test;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerMappingTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDuplicateEmail_returns409() {
        var ex = new DuplicateEmailException("Email already exists");

        ResponseEntity<ErrorResponse> response = handler.handleDuplicateEmail(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals("Email already exists", response.getBody().getMessage());
    }

    @Test
    void handleInvalidCredentials_returns401() {
        var ex = new InvalidCredentialsException("Invalid email or password");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Invalid email or password", response.getBody().getMessage());
    }

    @Test
    void handleAccessDenied_returns403() {
        var ex = new AccessDeniedException("You do not have permission");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
        assertEquals("You do not have permission", response.getBody().getMessage());
    }

    @Test
    void handleResourceNotFound_returns404() {
        var ex = new ResourceNotFoundException("Entity not found");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Entity not found", response.getBody().getMessage());
    }

    @Test
    void handleTooManyRequests_returns429() {
        var ex = new TooManyRequestsException("Rate limit exceeded");

        ResponseEntity<ErrorResponse> response = handler.handleTooManyRequestsException(ex);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals(429, response.getBody().getStatus());
        assertEquals("Too Many Requests", response.getBody().getError());
    }

    @Test
    void handleIllegalArgument_returns400() {
        var ex = new IllegalArgumentException("Invalid input");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid input", response.getBody().getMessage());
    }

    @Test
    void handlePropertyReferenceException_returns400() {
        var ex = mock(PropertyReferenceException.class);

        ResponseEntity<ErrorResponse> response = handler.handlePropertyReferenceException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid sorting parameter", response.getBody().getMessage());
    }

    @Test
    void handleMaxUploadSize_returns413() {
        var ex = new MaxUploadSizeExceededException(1024);

        ResponseEntity<ErrorResponse> response = handler.handleMaxSizeException(ex);

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertEquals(413, response.getBody().getStatus());
        assertEquals("Payload Too Large", response.getBody().getError());
        assertEquals("File too large", response.getBody().getMessage());
    }
}
