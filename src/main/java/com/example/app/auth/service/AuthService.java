package com.example.app.auth.service;

import com.example.app.auth.dto.AuthResponse;

public interface AuthService {
  AuthResponse login(String username, String password);

  AuthResponse refresh(String refreshToken);

  boolean validateToken(String token);

  void clearSession(String accessToken);

  String extractUsername(String token);

  String extractUsernameEvenIfExpired(String token);
}
