package com.dxc.iotmonitor.sensor.streetlight.controller;

import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorRequest;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorResponse;
import com.dxc.iotmonitor.sensor.streetlight.service.StreetLightSensorService;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sensors/street-lights")
public class StreetLightSensorController {

    private final StreetLightSensorService streetLightSensorService;

    @PostMapping
    public ResponseEntity<StreetLightSensorResponse> create(@Valid @RequestBody StreetLightSensorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(streetLightSensorService.save(request));
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
