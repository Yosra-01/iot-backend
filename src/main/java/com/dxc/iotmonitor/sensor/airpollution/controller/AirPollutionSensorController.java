package com.dxc.iotmonitor.sensor.airpollution.controller;

import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorRequest;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorResponse;
import com.dxc.iotmonitor.sensor.airpollution.service.AirPollutionSensorService;
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
@RequestMapping("/api/sensors/air-pollution")
public class AirPollutionSensorController {

    private final AirPollutionSensorService airPollutionSensorService;

    @PostMapping
    public ResponseEntity<AirPollutionSensorResponse> create(@Valid @RequestBody AirPollutionSensorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(airPollutionSensorService.save(request));
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
