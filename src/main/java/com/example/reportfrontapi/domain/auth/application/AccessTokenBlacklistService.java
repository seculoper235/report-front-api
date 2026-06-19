package com.example.reportfrontapi.domain.auth.application;

import com.example.reportfrontapi.web.security.JwtTokenProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

/**
 * 로그아웃된 access 토큰을 만료 시각까지 블랙리스트에 보관한다. key = "BL:{token}".
 * TTL은 토큰의 남은 만료시간으로 설정되어 만료 시 자동 소멸한다.
 */
@Service
public class AccessTokenBlacklistService {

    private static final String KEY_PREFIX = "BL:";
    private static final String BLACKLISTED = "logout";

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public AccessTokenBlacklistService(StringRedisTemplate redisTemplate, JwtTokenProvider jwtTokenProvider) {
        this.redisTemplate = redisTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void blacklist(String accessToken) {
        long remainingMillis = jwtTokenProvider.getExpiration(accessToken).getTime() - new Date().getTime();
        if (remainingMillis <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(key(accessToken), BLACKLISTED, Duration.ofMillis(remainingMillis));
    }

    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(accessToken)));
    }

    private String key(String accessToken) {
        return KEY_PREFIX + accessToken;
    }
}
