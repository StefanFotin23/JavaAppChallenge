package com.example.app.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String username,
        String tokenType
) {
    public AuthResponse(String accessToken, String refreshToken, String username) {
        this(accessToken, refreshToken, username, "Bearer");
    }
}
