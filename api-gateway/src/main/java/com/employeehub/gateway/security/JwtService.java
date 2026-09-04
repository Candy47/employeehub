package com.employeehub.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Central JWT validation for the whole system. The gateway is the single place
 * where incoming tokens are verified; downstream services trust the identity
 * the gateway forwards (via the X-Auth-User header) and never see the secret.
 */
@Service
public class JwtService {

    // Must match the secret auth-service uses to sign tokens.
    private static final String SECRET =
            "mySuperSecretKeyForJwtGenerationShouldBeAtLeast32CharactersLong";

    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    /** @return the subject (email) if the token is valid, otherwise null. */
    public String validateAndGetSubject(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (claims.getExpiration().before(new Date())) {
                return null;
            }
            return claims.getSubject();
        } catch (Exception ex) {
            return null;
        }
    }
}

