package com.example.reportfrontapi.domain.auth.controller.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
