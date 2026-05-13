package com.dxc.iotmonitor.sensor.traffic.controller;

import com.dxc.iotmonitor.security.JwtService;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorRequest;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorResponse;
import com.dxc.iotmonitor.sensor.traffic.service.TrafficSensorService;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sensors/traffic")
public class TrafficSensorController {

    private final TrafficSensorService trafficSensorService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    private Optional<User> resolveOptionalUser(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || auth.isBlank() || !auth.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = auth.substring(7).trim();
        if (token.isBlank()) {
            return Optional.empty();
        }
        String email = jwtService.extractUsername(token);
        return userRepository.findByEmailIgnoreCase(email);
    }

    @PostMapping
    public ResponseEntity<TrafficSensorResponse> create(
            HttpServletRequest request,
            @Valid @RequestBody TrafficSensorRequest body) {
        Optional<User> user = resolveOptionalUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(trafficSensorService.save(body, user));
    }

    @GetMapping
    public ResponseEntity<List<TrafficSensorResponse>> listAll() {
        return ResponseEntity.ok(trafficSensorService.getAll());
    }

    @GetMapping("/latest")
    public ResponseEntity<TrafficSensorResponse> getLatest() {
        return ResponseEntity.ok(trafficSensorService.getLatest());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrafficSensorResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(trafficSensorService.getById(id));
    }

    @DeleteMapping("/flush")
    public ResponseEntity<String> flush() {
        trafficSensorService.flush();
        return ResponseEntity.ok("Traffic sensor data flushed successfully.");
    }
}
