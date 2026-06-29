package com.dxc.iotmonitor.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBlacklistServiceTest {

    private final TokenBlacklistService service = new TokenBlacklistService();

    @Test
    void blacklistedToken_isBlacklistedReturnsTrue() {
        service.blacklist("token-123");
        assertTrue(service.isBlacklisted("token-123"));
    }

    @Test
    void unknownToken_isBlacklistedReturnsFalse() {
        assertFalse(service.isBlacklisted("unknown-token"));
    }
}
