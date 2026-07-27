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
    private final Map<String, Bucket> profileBuckets = new ConcurrentHashMap<>();

    private Bucket createBucket(long capacity, long refillTokens, Duration refillPeriod) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(refillTokens, refillPeriod)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    public boolean tryConsume(String endpoint, String ip) {
        Bucket bucket = switch (endpoint) {
            case "register" -> registerBuckets.computeIfAbsent(ip, k -> createBucket(10, 10, Duration.ofMinutes(1)));
            case "login" -> loginBuckets.computeIfAbsent(ip, k -> createBucket(10, 10, Duration.ofMinutes(1)));
            default -> null;
        };

        if (bucket == null) return true;
        return bucket.tryConsume(1);
    }

    public boolean tryConsumeProfile(String email) {
        Bucket bucket = profileBuckets.computeIfAbsent(email, k -> createBucket(10, 10, Duration.ofMinutes(1)));
        return bucket.tryConsume(1);
    }
}
