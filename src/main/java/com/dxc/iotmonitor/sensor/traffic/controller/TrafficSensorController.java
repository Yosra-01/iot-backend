package com.dxc.iotmonitor.sensor.traffic.controller;

import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorRequest;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorResponse;
import com.dxc.iotmonitor.sensor.traffic.service.TrafficSensorService;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.dxc.iotmonitor.enums.CongestionLevel;

import java.time.LocalDateTime;
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

    // THE UPDATED GET ENDPOINT FOR SPRINT 3
    @GetMapping
    public ResponseEntity<Page<TrafficSensorResponse>> listAll(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minDensity,
            @RequestParam(required = false) Integer maxDensity,
            @RequestParam(required = false) Float minSpeed,
            @RequestParam(required = false) Float maxSpeed,
            @RequestParam(required = false) CongestionLevel congestionLevel,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestampStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestampEnd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TrafficSensorResponse> response = trafficSensorService.getFilteredTrafficData(
                location, minDensity, maxDensity, minSpeed, maxSpeed,
                congestionLevel, timestampStart, timestampEnd, pageable
        );

        return ResponseEntity.ok(response);
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