package com.dxc.iotmonitor.polling;

import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/intervals")
public class PollingIntervalController {

    private final PollingIntervalService pollingIntervalService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<PollingIntervalResponse> getIntervals() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return ResponseEntity.ok(pollingIntervalService.getByUser(user));
    }

    @PutMapping
    public ResponseEntity<PollingIntervalResponse> upsert(
            @RequestBody @Valid PollingIntervalRequest body) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return ResponseEntity.ok(pollingIntervalService.upsert(user, body));
    }

    @DeleteMapping("/flush")
    public ResponseEntity<Map<String, String>> flush() {
        pollingIntervalService.flush();
        return ResponseEntity.ok(Map.of("message", "Polling intervals flushed successfully."));
    }
}
