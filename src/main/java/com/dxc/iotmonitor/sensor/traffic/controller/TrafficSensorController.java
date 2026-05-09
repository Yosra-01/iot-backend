package com.dxc.iotmonitor.sensor.traffic.controller;

import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorRequest;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorResponse;
import com.dxc.iotmonitor.sensor.traffic.service.TrafficSensorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sensors/traffic")
public class TrafficSensorController {

    private final TrafficSensorService trafficSensorService;

    @PostMapping
    public ResponseEntity<TrafficSensorResponse> create(@Valid @RequestBody TrafficSensorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trafficSensorService.save(request));
    }

    @GetMapping
    public ResponseEntity<List<TrafficSensorResponse>> listAll() {
            return ResponseEntity.ok(trafficSensorService.getAll());
    } 

    @DeleteMapping("/flush")
    public ResponseEntity<String> flush(){
        trafficSensorService.flush();
        return ResponseEntity.ok("Traffic table flushed ");
    }
}
