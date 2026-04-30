package com.dxc.iotmonitor.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();

    private Bucket createRegisterBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillIntervally(10, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createLoginBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillIntervally(10, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    public boolean tryConsume(String endpoint, String ip) {
        Bucket bucket = switch (endpoint) {
            case "register" -> registerBuckets.computeIfAbsent(ip, k -> createRegisterBucket());
            case "login" -> loginBuckets.computeIfAbsent(ip, k -> createLoginBucket());
            default -> null;
        };

        if (bucket == null) return true;
        return bucket.tryConsume(1);
    }
}
