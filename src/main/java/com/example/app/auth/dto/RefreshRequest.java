package com.example.app.auth.dto;

import jakarta.validation.constraints.NotNull;

public record RefreshRequest(
        @NotNull(message = "Refresh token cannot be null")
        String refreshToken
) {}
