package com.dxc.iotmonitor.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //409 Conflict
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),       // 409
                "Conflict",
                e.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    //401 Unauthorized
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),   // 401
                "Unauthorized",
                e.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    //404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),   // 404
                "Not Found",
                e.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    //429 Too Many Requests
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequestsException(TooManyRequestsException e){
        ErrorResponse error = new ErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too Many Requests",
                e.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.TOO_MANY_REQUESTS);
    }

    //400 Bad Request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException e) {
        List<String> messages = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                messages
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    //400 Bad Request - for @RequestBody @Valid List and other method validation (Spring Boot 3)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        List<String> messages = e.getAllErrors().stream()
                .map(this::resolveValidationMessage)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (messages.isEmpty()) {
            messages = List.of("Validation failed");
        }

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                messages
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    private String resolveValidationMessage(MessageSourceResolvable error) {
        if (error instanceof FieldError fieldError) {
            return fieldError.getDefaultMessage();
        }
        String message = error.getDefaultMessage();
        if (message != null) {
            return message;
        }
        if (error instanceof DefaultMessageSourceResolvable resolvable) {
            String[] codes = resolvable.getCodes();
            if (codes != null && codes.length > 0) {
                return codes[0];
            }
        }
        return error.toString();
    }

    //400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                e.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    //400 Bad Request
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                resolveHttpMessageNotReadableMessage(e)
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    private String resolveHttpMessageNotReadableMessage(HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof InvalidFormatException ife && ife.getTargetType() != null
                    && ife.getTargetType().isEnum()) {
                return "invalid metric for this sensor type";
            }
            if (cause instanceof MismatchedInputException) {
                return "Invalid value for field.";
            }
            if (cause instanceof InvalidFormatException) {
                return "Invalid value for field.";
            }
            String msg = cause.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (lower.contains("enum") && lower.contains("value")) {
                    return "invalid metric for this sensor type";
                }
                if (lower.contains("floating point") || lower.contains("integral type")) {
                    return "Invalid value for field.";
                }
            }
            cause = cause.getCause();
        }
        return "Invalid value for field.";
    }

    //400 Bad Request - invalid UUID in path variable
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Invalid UUID string: " + e.getValue()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    //403 Forbidden
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                e.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // 400 Bad Request - Invalid Sorting Field
    @ExceptionHandler(org.springframework.data.mapping.PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReferenceException(org.springframework.data.mapping.PropertyReferenceException e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Invalid sorting parameter" // Exact message expected by QA
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 413 Payload Too Large
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(MaxUploadSizeExceededException e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.PAYLOAD_TOO_LARGE.value(), // 413
                "Payload Too Large",
                "File too large"
        );
        return new ResponseEntity<>(error, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    //500 Internal Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), // 500
                "Internal Server Error",
                "Internal error occurred."
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}