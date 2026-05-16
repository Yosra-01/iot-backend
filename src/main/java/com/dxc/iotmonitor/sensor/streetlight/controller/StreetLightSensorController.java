package com.dxc.iotmonitor.sensor.streetlight.controller;

import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorRequest;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorResponse;
import com.dxc.iotmonitor.sensor.streetlight.service.StreetLightSensorService;
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
@RequestMapping("/api/sensors/street-lights")
public class StreetLightSensorController {

    private final StreetLightSensorService streetLightSensorService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<StreetLightSensorResponse> create(
            @Valid @RequestBody StreetLightSensorRequest body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> user = (auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken))
                ? userRepository.findByEmailIgnoreCase(auth.getName())
                : Optional.empty();
        return ResponseEntity.status(HttpStatus.CREATED).body(streetLightSensorService.save(body, user));
    }

    @GetMapping
    public ResponseEntity<List<StreetLightSensorResponse>> listAll() {
        return ResponseEntity.ok(streetLightSensorService.getAll());
    }

    @GetMapping("/latest")
    public ResponseEntity<StreetLightSensorResponse> getLatest() {
        return ResponseEntity.ok(streetLightSensorService.getLatest());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StreetLightSensorResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(streetLightSensorService.getById(id));
    }

    @DeleteMapping("/flush")
    public ResponseEntity<String> flush() {
        streetLightSensorService.flush();
        return ResponseEntity.ok("Street light sensor data flushed successfully.");
    }
}
