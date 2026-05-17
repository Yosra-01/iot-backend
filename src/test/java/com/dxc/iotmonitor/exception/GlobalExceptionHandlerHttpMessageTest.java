package com.dxc.iotmonitor.exception;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerHttpMessageTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleHttpMessageNotReadable_fractionalInteger_returnsInvalidValueForField() {
        MismatchedInputException cause = MismatchedInputException.from(
                null,
                Integer.class,
                "Cannot deserialize value of type `java.lang.Integer` from floating point number (5.5)");
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error", cause);

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid value for field.", response.getBody().getMessage());
    }
}
