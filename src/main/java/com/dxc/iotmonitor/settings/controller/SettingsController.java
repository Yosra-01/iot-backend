package com.dxc.iotmonitor.settings.controller;

import com.dxc.iotmonitor.security.JwtService;
import com.dxc.iotmonitor.settings.dto.SettingsRequest;
import com.dxc.iotmonitor.settings.dto.SettingsResponse;
import com.dxc.iotmonitor.settings.service.SettingsService;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/settings")
@Slf4j
public class SettingsController {

    private final SettingsService settingsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    private User getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String email = jwtService.extractUsername(token);
        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PutMapping
    public ResponseEntity<List<SettingsResponse>> upsert(
            HttpServletRequest request,
            @RequestBody @Valid List<SettingsRequest> requests) {
        User user = getCurrentUser(request);
        return ResponseEntity.ok(settingsService.upsert(requests, user));
    }

    @GetMapping
    public ResponseEntity<List<SettingsResponse>> findAll(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return ResponseEntity.ok(settingsService.findAll(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteById(
            @PathVariable String id,
            HttpServletRequest request) {
        User user = getCurrentUser(request);
        settingsService.deleteById(id, user);
        return ResponseEntity.ok(Map.of("message", "Setting deleted successfully."));
    }

    @DeleteMapping("/flush")
    public ResponseEntity<Map<String, String>> flush() {
        settingsService.flush();
        return ResponseEntity.ok(Map.of("message", "Settings flushed successfully."));
    }
}
