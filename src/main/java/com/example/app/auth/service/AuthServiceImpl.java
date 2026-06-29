package com.example.app.auth.service;

import com.example.app.auth.dto.AuthResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final static Integer accessTokenExpirationMinutes = 15;
    private final static Integer refreshTokenExpirationMinutes = 1440;

    @Value("${jwt.secret}")
    private String secret;

    private final Map<String, String> refreshTokens = new ConcurrentHashMap<>();

    @Override
    public AuthResponse login(String username, String password) {
        String accessToken = generateToken(username, accessTokenExpirationMinutes);
        String refreshToken = generateToken(username, refreshTokenExpirationMinutes);

        refreshTokens.put(refreshToken, username);
        return new AuthResponse(accessToken, refreshToken, username);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        if (refreshTokens.containsKey(refreshToken) && validateToken(refreshToken)) {
            String username = refreshTokens.get(refreshToken);
            String newAccessToken = generateToken(username, accessTokenExpirationMinutes);
            return new AuthResponse(newAccessToken, refreshToken, username);
        }
        throw new RuntimeException("Invalid refresh token");
    }

    @Override
    public String getUsernameFromToken(String token) {
        return extractUsername(token);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void clearSession(String accessToken) {
        String username = extractUsername(accessToken);
        refreshTokens.values().removeIf(val -> val.equals(username));
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private String generateToken(String username, int minutes) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (long) minutes * 60 * 1000))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
