package com.dxc.iotmonitor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "this-is-a-test-secret-that-is-at-least-32-bytes-long-for-hs256");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken("test@example.com");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractEmail_returnsSubject() {
        String token = jwtUtil.generateToken("test@example.com");
        assertEquals("test@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void isTokenValid_withValidToken_returnsTrue() {
        String token = jwtUtil.generateToken("test@example.com");
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_withExpiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1L);
        String token = jwtUtil.generateToken("test@example.com");

        assertFalse(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_withTamperedToken_returnsFalse() {
        String token = jwtUtil.generateToken("test@example.com");
        String tampered = token.substring(0, token.length() - 4) + "XXXX";

        assertFalse(jwtUtil.isTokenValid(tampered));
    }

    @Test
    void isTokenValid_withNullToken_returnsFalse() {
        assertFalse(jwtUtil.isTokenValid(null));
    }

    @Test
    void isTokenValid_withEmptyToken_returnsFalse() {
        assertFalse(jwtUtil.isTokenValid(""));
    }
}
