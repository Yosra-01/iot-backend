package com.dxc.iotmonitor.settings.controller;

import com.dxc.iotmonitor.settings.dto.ThresholdSettingRequest;
import com.dxc.iotmonitor.settings.dto.ThresholdSettingResponse;
import com.dxc.iotmonitor.settings.service.ThresholdSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/settings")
@Slf4j
public class ThresholdSettingController {

    private final ThresholdSettingService thresholdSettingService;

    @PutMapping
    public ResponseEntity<List<ThresholdSettingResponse>> upsert(
            @RequestBody @Valid List<ThresholdSettingRequest> requests) {
        return ResponseEntity.ok(thresholdSettingService.upsert(requests));
    }

    @GetMapping
    public ResponseEntity<List<ThresholdSettingResponse>> findAll() {
        return ResponseEntity.ok(thresholdSettingService.findAll());
    }

    @DeleteMapping("/flush")
    public ResponseEntity<Map<String, String>> flush() {
        thresholdSettingService.flush();
        return ResponseEntity.ok(Map.of("message", "Settings flushed successfully."));
    }
}
