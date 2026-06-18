package com.example.reportfrontapi.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    /**
     * crt_by / udt_by 값을 채우는 감사자 제공자.
     * 인증 기능 도입 전까지는 시스템 사용자(0L)로 고정한다.
     * TODO: SecurityContext의 로그인 사용자 ID로 교체할 것.
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> Optional.of(0L);
    }
}
