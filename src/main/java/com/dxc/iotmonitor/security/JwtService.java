package com.dxc.iotmonitor.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtUtil jwtUtil;

    public String extractUsername(String token) {
        return jwtUtil.extractEmail(token);
    }
}
