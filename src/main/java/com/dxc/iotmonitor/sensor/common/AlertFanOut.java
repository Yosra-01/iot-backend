package com.dxc.iotmonitor.sensor.common;

import com.dxc.iotmonitor.alert.service.AlertService;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertFanOut {

    private final AlertService alertService;
    private final UserRepository userRepository;

    public void fanOut(SensorType type, Map<Metric, Float> readings, String location, Optional<User> user, UUID readingId) {
        if (user.isPresent()) {
            alertService.checkAndTrigger(type, readings, location, user.get(), readingId);
        } else {
            for (User u : userRepository.findAll()) {
                alertService.checkAndTrigger(type, readings, location, u, readingId);
            }
        }
    }
}
