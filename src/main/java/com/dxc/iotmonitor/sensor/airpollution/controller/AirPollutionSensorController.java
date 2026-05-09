package com.dxc.iotmonitor.sensor.airpollution.controller;

import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorRequest;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorResponse;
import com.dxc.iotmonitor.sensor.airpollution.service.AirPollutionSensorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/sensors/airpollution")
@RestController
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

    @DeleteMapping("/flush")
    public ResponseEntity<String> flush() {
        airPollutionSensorService.flush();
        return ResponseEntity.ok("Air pollution table flushed successfully");
    }
}
