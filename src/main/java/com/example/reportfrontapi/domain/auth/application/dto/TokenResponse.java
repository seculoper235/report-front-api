package com.example.reportfrontapi.domain.auth.application.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
