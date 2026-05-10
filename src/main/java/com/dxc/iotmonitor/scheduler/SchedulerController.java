package com.dxc.iotmonitor.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sensors")
public class SchedulerController {

    private final SensorScheduler sensorScheduler;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generate() {
        log.info("[SchedulerController][generate] start");
        sensorScheduler.sendTrafficReading();
        sensorScheduler.sendAirPollutionReading();
        sensorScheduler.sendStreetLightReading();
        log.info("[SchedulerController][generate] end");
        return ResponseEntity.ok(Map.of("message", "Sensor data generated successfully."));
    }
}
