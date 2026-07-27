package com.dxc.iotmonitor.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // converts the raw secret string into a cryptographic SecretKey used for signing and verifying
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // generates a JWT token with the email as the subject
    public String generateToken(String email) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(expiration);
        // Use numeric JWT iat/exp (epoch seconds) so we never touch java.util.Date (java:S2143).
        // JJWT's builder Date overloads are library-only; claim() stores the same standard fields.
        return Jwts.builder()
                .subject(email)
                .claim(Claims.ISSUED_AT, now.getEpochSecond())
                .claim(Claims.EXPIRATION, expiresAt.getEpochSecond())
                .signWith(getSigningKey(), Jwts.SIG.HS256) // signs with HMAC-SHA256
                .compact(); // builds and returns the token string
    }

    // extracts all claims (payload data) from the token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // uses the same key to verify the signature
                .build()
                .parseSignedClaims(token) // parses and validates the token
                .getPayload(); // returns the claims inside the token
    }

    // extracts the email (subject) from the token
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // checks if the token has passed its expiration time
    private boolean isTokenExpired(String token) {
        Number exp = extractAllClaims(token).get(Claims.EXPIRATION, Number.class);
        if (exp == null) {
            return true;
        }
        return Instant.ofEpochSecond(exp.longValue()).isBefore(Instant.now());
    }

    // returns true if the token is valid — not expired and signature is intact
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token); // if parsing succeeds and not expired, it's valid
        } catch (Exception e) {
            return false; // any exception means the token is invalid or tampered
        }
    }
}
