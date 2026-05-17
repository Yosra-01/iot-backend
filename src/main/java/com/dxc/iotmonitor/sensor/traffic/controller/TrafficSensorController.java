package com.dxc.iotmonitor.sensor.traffic.controller;

import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorRequest;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorResponse;
import com.dxc.iotmonitor.sensor.traffic.service.TrafficSensorService;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<TrafficSensorResponse> create(
            @Valid @RequestBody TrafficSensorRequest body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> user = (auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken))
                ? userRepository.findByEmailIgnoreCase(auth.getName())
                : Optional.empty();
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
