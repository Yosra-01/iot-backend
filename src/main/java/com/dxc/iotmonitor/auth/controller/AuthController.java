package com.dxc.iotmonitor.auth.controller;

import com.dxc.iotmonitor.auth.dto.LoginRequest;
import com.dxc.iotmonitor.auth.dto.SignupRequest;
import com.dxc.iotmonitor.auth.dto.AuthResponse;
import com.dxc.iotmonitor.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    //SIGN-UP
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> createUser(@RequestBody @Valid SignupRequest request) {
        AuthResponse newUser = authService.createUser(request);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    //SIGN-IN
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request){
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
