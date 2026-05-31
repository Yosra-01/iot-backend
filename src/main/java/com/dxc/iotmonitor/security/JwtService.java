package com.dxc.iotmonitor.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtService {

    private final JwtUtil jwtUtil;

    public String extractUsername(String token) {
        return jwtUtil.extractEmail(token);
    }
}
