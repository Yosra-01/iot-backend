package com.dxc.iotmonitor.polling;

import com.dxc.iotmonitor.security.JwtService;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/intervals")
public class PollingIntervalController {

    private final PollingIntervalService pollingIntervalService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    private User getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String email = jwtService.extractUsername(token);
        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<PollingIntervalResponse> getIntervals(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return ResponseEntity.ok(pollingIntervalService.getByUser(user));
    }

    @PutMapping
    public ResponseEntity<PollingIntervalResponse> upsert(
            HttpServletRequest request,
            @RequestBody @Valid PollingIntervalRequest body) {
        User user = getCurrentUser(request);
        return ResponseEntity.ok(pollingIntervalService.upsert(user, body));
    }
}
