package com.utgaming.testconcurrency.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
    private final String secret;
    private final long expireSeconds;
    private final String issuer;

    public JwtUtil(@Value("${security.jwt.issuer}")  String issuer
            ,@Value("${security.jwt.secret}") String secret,
                   @Value("${security.jwt.expire-seconds}") long expireSeconds) {
        this.secret = secret;
        this.expireSeconds = expireSeconds;
        this.issuer = issuer;
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expireSeconds);
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .claim("username",username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(getKey(),Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseClaimsJws(token)
                .getPayload();
    }
}
