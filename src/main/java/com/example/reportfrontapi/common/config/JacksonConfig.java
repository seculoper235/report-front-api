package com.example.reportfrontapi.common.config;

import com.example.reportfrontapi.common.code.CodeEnumModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JacksonModule;

@Configuration
public class JacksonConfig {

    // CodeEnum을 code 값으로 (역)직렬화하도록 등록. Spring Boot가 JacksonModule 빈을 자동 적용한다.
    @Bean
    public JacksonModule codeEnumModule() {
        return new CodeEnumModule();
    }
}
