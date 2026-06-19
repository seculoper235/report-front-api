package com.example.reportfrontapi.common.config;

import com.example.reportfrontapi.web.security.SecurityUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    private static final Long SYSTEM_USER_ID = 0L;

    /**
     * crt_by / udt_by 값을 채우는 감사자 제공자.
     * SecurityContext의 로그인 사용자 ID를 사용하고, 인증 정보가 없으면(로그인/가입 등)
     * 시스템 사용자(0L)로 폴백한다.
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> Optional.of(SecurityUtil.getCurrentUserId().orElse(SYSTEM_USER_ID));
    }
}
