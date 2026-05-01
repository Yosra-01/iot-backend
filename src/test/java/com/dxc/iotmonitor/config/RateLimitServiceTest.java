package com.dxc.iotmonitor.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
    }

    @Test
    void register_shouldAllowFirst10Requests() {
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimitService.tryConsume("register", "192.168.1.1"),
                    "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    void register_shouldBlockAfter10Requests() {
        for (int i = 0; i < 10; i++) {
            rateLimitService.tryConsume("register", "192.168.1.1");
        }
        assertFalse(rateLimitService.tryConsume("register", "192.168.1.1"),
                "11th request should be blocked");
    }

    @Test
    void login_shouldAllowFirst10Requests() {
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimitService.tryConsume("login", "192.168.1.1"),
                    "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    void login_shouldBlockAfter10Requests() {
        for (int i = 0; i < 10; i++) {
            rateLimitService.tryConsume("login", "192.168.1.1");
        }
        assertFalse(rateLimitService.tryConsume("login", "192.168.1.1"),
                "11th request should be blocked");
    }

    @Test
    void differentIPs_shouldHaveIndependentBuckets() {
        for (int i = 0; i < 10; i++) {
            rateLimitService.tryConsume("login", "192.168.1.1");
        }
        assertTrue(rateLimitService.tryConsume("login", "192.168.1.2"),
                "Different IP should not be affected");
    }

    @Test
    void unknownEndpoint_shouldAlwaysAllow() {
        for (int i = 0; i < 20; i++) {
            assertTrue(rateLimitService.tryConsume("unknown", "192.168.1.1"),
                    "Unknown endpoint should always be allowed");
        }
    }
}
