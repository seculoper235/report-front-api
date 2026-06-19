package com.example.reportfrontapi.domain.auth.application;

import com.example.reportfrontapi.web.security.JwtProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * refresh 토큰을 Redis에 저장/조회/삭제한다. key = "RT:{userId}".
 */
@Service
public class RefreshTokenService {

    private static final String KEY_PREFIX = "RT:";

    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;

    public RefreshTokenService(StringRedisTemplate redisTemplate, JwtProperties jwtProperties) {
        this.redisTemplate = redisTemplate;
        this.ttl = Duration.ofMillis(jwtProperties.refreshExpirationMilliseconds());
    }

    public void save(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(key(userId), refreshToken, ttl);
    }

    public Optional<String> find(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(userId)));
    }

    public void delete(Long userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}
