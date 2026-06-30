package com.example.app.auth.service;

import com.example.app.auth.dto.AuthResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Set;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private static final Integer accessTokenExpirationMinutes = 15;
  private static final Integer refreshTokenExpirationMinutes = 1440;

  @Value("${jwt.secret}")
  private String secret;

  private final StringRedisTemplate redisTemplate;

  @Override
  public AuthResponse login(String username, String password) {
    String accessToken = generateToken(username, accessTokenExpirationMinutes);
    String refreshToken = generateToken(username, refreshTokenExpirationMinutes);

    redisTemplate
        .opsForValue()
        .set(
            "refresh_token:" + refreshToken,
            username,
            Duration.ofMinutes(refreshTokenExpirationMinutes));
    return new AuthResponse(accessToken, refreshToken, username);
  }

  @Override
  public AuthResponse refresh(String refreshToken) {
    String username = redisTemplate.opsForValue().get("refresh_token:" + refreshToken);
    if (username != null && validateToken(refreshToken)) {
      String newAccessToken = generateToken(username, accessTokenExpirationMinutes);
      return new AuthResponse(newAccessToken, refreshToken, username);
    }
    throw new RuntimeException("Invalid refresh token");
  }

  @Override
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public void clearSession(String accessToken) {
    String username = extractUsernameEvenIfExpired(accessToken);
    if (username == null) {
      return;
    }
    Set<String> keys = redisTemplate.keys("refresh_token:*");
    if (keys != null) {
      for (String key : keys) {
        if (username.equals(redisTemplate.opsForValue().get(key))) {
          redisTemplate.delete(key);
        }
      }
    }
  }

  @Override
  public String extractUsername(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  @Override
  public String extractUsernameEvenIfExpired(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(getSigningKey())
          .build()
          .parseClaimsJws(token)
          .getBody()
          .getSubject();
    } catch (io.jsonwebtoken.ExpiredJwtException e) {
      return e.getClaims().getSubject();
    } catch (Exception e) {
      return null;
    }
  }

  private String generateToken(String username, int minutes) {
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + (long) minutes * 60 * 1000))
        .signWith(getSigningKey())
        .compact();
  }

  private SecretKey getSigningKey() {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
