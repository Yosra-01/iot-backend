package com.dxc.iotmonitor.sensor.airpollution.controller;

import com.dxc.iotmonitor.enums.PollutionLevel;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionFilterParams;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorRequest;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorResponse;
import com.dxc.iotmonitor.sensor.airpollution.service.AirPollutionSensorHandler;
import com.dxc.iotmonitor.sensor.common.AuthenticatedUserResolver;
import com.dxc.iotmonitor.sensor.common.PageRequestBuilder;
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
@RequestMapping("/api/sensors/air-pollution")
public class AirPollutionSensorController {

    private final AirPollutionSensorHandler airPollutionSensorHandler;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @PostMapping
    public ResponseEntity<AirPollutionSensorResponse> create(
            @Valid @RequestBody AirPollutionSensorRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(airPollutionSensorHandler.save(body, authenticatedUserResolver.current()));
    }

    @GetMapping
    public ResponseEntity<Page<AirPollutionSensorResponse>> listAll(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Float minPm2_5,
            @RequestParam(required = false) Float maxPm2_5,
            @RequestParam(required = false) Float minPm10,
            @RequestParam(required = false) Float maxPm10,
            @RequestParam(required = false) Float minCo,
            @RequestParam(required = false) Float maxCo,
            @RequestParam(required = false) Float minNo2,
            @RequestParam(required = false) Float maxNo2,
            @RequestParam(required = false) Float minSo2,
            @RequestParam(required = false) Float maxSo2,
            @RequestParam(required = false) Float minOzone,
            @RequestParam(required = false) Float maxOzone,
            @RequestParam(required = false) PollutionLevel pollutionLevel,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestampStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestampEnd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        AirPollutionFilterParams filters = new AirPollutionFilterParams(
                location, minPm2_5, maxPm2_5, minPm10, maxPm10,
                minCo, maxCo, minNo2, maxNo2, minSo2, maxSo2,
                minOzone, maxOzone, pollutionLevel, timestampStart, timestampEnd
        );
        Pageable pageable = PageRequestBuilder.from(page, size, sortBy, sortDir);
        Page<AirPollutionSensorResponse> response = airPollutionSensorHandler.getFiltered(filters, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest")
    public ResponseEntity<AirPollutionSensorResponse> getLatest() {
        return ResponseEntity.ok(airPollutionSensorHandler.getLatest());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirPollutionSensorResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(airPollutionSensorHandler.getById(id));
    }

    @DeleteMapping("/flush")
    public ResponseEntity<String> flush() {
        airPollutionSensorHandler.flush();
        return ResponseEntity.ok("Air pollution sensor data flushed successfully.");
    }
}
