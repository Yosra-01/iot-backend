package com.dxc.iotmonitor.sensor.traffic.controller;

import com.dxc.iotmonitor.enums.CongestionLevel;
import com.dxc.iotmonitor.sensor.common.AuthenticatedUserResolver;
import com.dxc.iotmonitor.sensor.common.PageRequestBuilder;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficFilterParams;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorRequest;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorResponse;
import com.dxc.iotmonitor.sensor.traffic.service.TrafficSensorHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sensors/traffic")
public class TrafficSensorController {

    private final TrafficSensorHandler trafficSensorHandler;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @PostMapping
    public ResponseEntity<TrafficSensorResponse> create(
            @Valid @RequestBody TrafficSensorRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trafficSensorHandler.save(body, authenticatedUserResolver.current()));
    }

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

        TrafficFilterParams filters = new TrafficFilterParams(
                location, minDensity, maxDensity, minSpeed, maxSpeed,
                congestionLevel, timestampStart, timestampEnd
        );
        Pageable pageable = PageRequestBuilder.from(page, size, sortBy, sortDir);
        Page<TrafficSensorResponse> response = trafficSensorHandler.getFiltered(filters, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest")
    public ResponseEntity<TrafficSensorResponse> getLatest() {
        return ResponseEntity.ok(trafficSensorHandler.getLatest());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrafficSensorResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(trafficSensorHandler.getById(id));
    }

    @DeleteMapping("/flush")
    public ResponseEntity<String> flush() {
        trafficSensorHandler.flush();
        return ResponseEntity.ok("Traffic sensor data flushed successfully.");
    }
}
