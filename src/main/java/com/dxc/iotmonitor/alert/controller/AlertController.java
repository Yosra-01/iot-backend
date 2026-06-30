package com.dxc.iotmonitor.alert.controller;

import com.dxc.iotmonitor.alert.dto.AlertFilterParams;
import com.dxc.iotmonitor.alert.dto.response.AlertResponse;
import com.dxc.iotmonitor.alert.service.AlertService;
import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.sensor.common.AuthenticatedUserResolver;
import com.dxc.iotmonitor.sensor.common.PageRequestBuilder;
import com.dxc.iotmonitor.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @GetMapping
    public ResponseEntity<Page<AlertResponse>> findAll(
            @RequestParam(required = false) SensorType sensorType,
            @RequestParam(required = false) Metric metric,
            @RequestParam(required = false) AlertType alertType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime triggeredStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime triggeredEnd,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "triggeredAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        User user = authenticatedUserResolver.current()
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        AlertFilterParams filters = new AlertFilterParams(
                sensorType, metric, alertType, location, triggeredStart, triggeredEnd, read);
        Pageable pageable = PageRequestBuilder.from(page, size, sortBy, sortDir);
        return ResponseEntity.ok(alertService.findFiltered(filters, pageable, user));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count(
            @RequestParam(required = false) SensorType sensorType,
            @RequestParam(required = false) Metric metric,
            @RequestParam(required = false) AlertType alertType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime triggeredStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime triggeredEnd,
            @RequestParam(required = false) Boolean read) {

        User user = authenticatedUserResolver.current()
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        AlertFilterParams filters = new AlertFilterParams(
                sensorType, metric, alertType, location, triggeredStart, triggeredEnd, read);
        return ResponseEntity.ok(Map.of("count", alertService.count(filters, user)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> findById(@PathVariable UUID id) {
        User user = authenticatedUserResolver.current()
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return ResponseEntity.ok(alertService.findById(id, user));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable UUID id) {
        User user = authenticatedUserResolver.current()
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        alertService.markAsRead(id, user);
        return ResponseEntity.ok(Map.of("message", "Alert marked as read."));
    }

    @DeleteMapping("/flush")
    public ResponseEntity<Map<String, String>> flush() {
        alertService.flush();
        return ResponseEntity.ok(Map.of("message", "Alerts flushed successfully."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteById(@PathVariable UUID id) {
        User user = authenticatedUserResolver.current()
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        alertService.deleteById(id, user);
        return ResponseEntity.ok(Map.of("message", "Alert dismissed successfully."));
    }
}
