package com.example.reportfrontapi.web.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * SecurityContext에서 현재 로그인 사용자 ID를 조회한다.
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static Optional<Long> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            return Optional.empty();
        }
        return Optional.of(userId);
    }
}
