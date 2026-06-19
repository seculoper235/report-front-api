package com.example.reportfrontapi.web.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * jwt.* 설정 바인딩.
 * EXPIRATION_MILLISECONDS / REFRESH_EXPIRATION_MILLISECONDS 는 relaxed binding으로 매핑된다.
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long expirationMilliseconds,
        long refreshExpirationMilliseconds
) {
}
