package com.dxc.iotmonitor.auth.controller;

import com.dxc.iotmonitor.auth.dto.LoginRequest;
import com.dxc.iotmonitor.auth.dto.SignupRequest;
import com.dxc.iotmonitor.auth.dto.AuthResponse;
import com.dxc.iotmonitor.auth.service.AuthService;
// ADD THESE TWO IMPORTS:
import com.dxc.iotmonitor.config.RateLimitService;
import com.dxc.iotmonitor.exception.TooManyRequestsException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;
    private final RateLimitService rateLimitService; // 1. INJECT THE RATE LIMITER

    // SIGN-UP
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> createUser(@RequestBody @Valid SignupRequest request) throws TooManyRequestsException {
        // 2. CHECK RATE LIMIT BEFORE PROCESSING
        if (!rateLimitService.tryConsumeProfile(request.getEmail())) {
            throw new TooManyRequestsException("Too many requests. Please try again later.");
        }

        AuthResponse newUser = authService.createUser(request);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    // SIGN-IN
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) throws TooManyRequestsException {
        // 3. CHECK RATE LIMIT BEFORE PROCESSING
        if (!rateLimitService.tryConsumeProfile(request.getEmail())) {
            throw new TooManyRequestsException("Too many requests. Please try again later.");
        }

        AuthResponse existingUser = authService.login(request);
        return new ResponseEntity<>(existingUser, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }
        String token = authHeader.substring(7);
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }
}