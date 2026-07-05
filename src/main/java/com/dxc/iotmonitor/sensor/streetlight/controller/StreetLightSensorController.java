package com.dxc.iotmonitor.sensor.streetlight.controller;

import com.dxc.iotmonitor.enums.LightStatus;
import com.dxc.iotmonitor.sensor.common.AuthenticatedUserResolver;
import com.dxc.iotmonitor.sensor.common.PageRequestBuilder;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightFilterParams;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorRequest;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorResponse;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightStatsResponse;
import com.dxc.iotmonitor.sensor.streetlight.service.StreetLightSensorHandler;
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
@RequestMapping("/api/sensors/street-lights")
public class StreetLightSensorController {

    private final StreetLightSensorHandler streetLightSensorHandler;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @PostMapping
    public ResponseEntity<StreetLightSensorResponse> create(
            @Valid @RequestBody StreetLightSensorRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(streetLightSensorHandler.save(body, authenticatedUserResolver.current()));
    }

    @GetMapping
    public ResponseEntity<Page<StreetLightSensorResponse>> listAll(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minBrightness,
            @RequestParam(required = false) Integer maxBrightness,
            @RequestParam(required = false) Float minPower,
            @RequestParam(required = false) Float maxPower,
            @RequestParam(required = false) LightStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestampStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestampEnd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        StreetLightFilterParams filters = new StreetLightFilterParams(
                location, minBrightness, maxBrightness, minPower, maxPower,
                status, timestampStart, timestampEnd
        );
        Pageable pageable = PageRequestBuilder.from(page, size, sortBy, sortDir);
        Page<StreetLightSensorResponse> response = streetLightSensorHandler.getFiltered(filters, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<StreetLightStatsResponse> stats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(streetLightSensorHandler.getStats(from, to, location));
    }

    @GetMapping("/latest")
    public ResponseEntity<StreetLightSensorResponse> getLatest() {
        return ResponseEntity.ok(streetLightSensorHandler.getLatest());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StreetLightSensorResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(streetLightSensorHandler.getById(id));
    }

    @DeleteMapping("/flush")
    public ResponseEntity<String> flush() {
        streetLightSensorHandler.flush();
        return ResponseEntity.ok("Street light sensor data flushed successfully.");
    }
}
