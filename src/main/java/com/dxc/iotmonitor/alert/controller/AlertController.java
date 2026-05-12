package com.dxc.iotmonitor.alert.controller;

import com.dxc.iotmonitor.alert.dto.response.AlertResponse;
import com.dxc.iotmonitor.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<List<AlertResponse>> findAll() {
        return ResponseEntity.ok(alertService.findAll());
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count() {
        return ResponseEntity.ok(Map.of("count", alertService.count()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(alertService.findById(id));
    }

    @DeleteMapping("/flush")
    public ResponseEntity<Map<String, String>> flush() {
        alertService.flush();
        return ResponseEntity.ok(Map.of("message", "Alerts flushed successfully."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteById(@PathVariable UUID id) {
        alertService.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Alert dismissed successfully."));
    }
}
