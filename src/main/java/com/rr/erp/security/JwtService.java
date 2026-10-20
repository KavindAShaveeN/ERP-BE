package com.rr.erp.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

/** Issues and validates the login token. It carries only the employee code; roles and
 * permissions are read from the database on each request so changes apply immediately. */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration expiry;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiry-hours:12}") long expiryHours
    ) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expiry = Duration.ofHours(expiryHours);
    }

    public String issue(String employeeCode) {
        Date now = new Date();
        return Jwts.builder()
                .subject(employeeCode)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiry.toMillis()))
                .signWith(key)
                .compact();
    }

    /** The employee code the token was issued to, or empty if it is invalid or expired. */
    public Optional<String> validate(String token) {
        try {
            return Optional.ofNullable(
                    Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
