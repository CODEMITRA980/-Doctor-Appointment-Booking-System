package com.luminacare.authservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:aVeryLongSecretKeyForJwtSignatureValidationThatIsAtLeast32Bytes}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expirationTime;

    public String generateToken(String username, String role, Long id) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("userId", id)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }
}
