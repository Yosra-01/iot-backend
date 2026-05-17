package com.dxc.iotmonitor.sensor.airpollution.controller;

import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorRequest;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorResponse;
import com.dxc.iotmonitor.sensor.airpollution.service.AirPollutionSensorService;
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
@RequestMapping("/api/sensors/air-pollution")
public class AirPollutionSensorController {

    private final AirPollutionSensorService airPollutionSensorService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<AirPollutionSensorResponse> create(
            @Valid @RequestBody AirPollutionSensorRequest body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> user = (auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken))
                ? userRepository.findByEmailIgnoreCase(auth.getName())
                : Optional.empty();
        return ResponseEntity.status(HttpStatus.CREATED).body(airPollutionSensorService.save(body, user));
    }

    @GetMapping
    public ResponseEntity<List<AirPollutionSensorResponse>> listAll() {
        return ResponseEntity.ok(airPollutionSensorService.getAll());
    }

    @GetMapping("/latest")
    public ResponseEntity<AirPollutionSensorResponse> getLatest() {
        return ResponseEntity.ok(airPollutionSensorService.getLatest());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirPollutionSensorResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(airPollutionSensorService.getById(id));
    }

    @DeleteMapping("/flush")
    public ResponseEntity<String> flush() {
        airPollutionSensorService.flush();
        return ResponseEntity.ok("Air pollution sensor data flushed successfully.");
    }
}
