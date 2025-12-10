package com.yimusi.config;

import java.util.Optional;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;

@TestConfiguration
public class TestAuditorConfig {

    private static final ThreadLocal<Long> CURRENT_AUDITOR = new ThreadLocal<>();

    public static void setAuditor(Long auditor) {
        CURRENT_AUDITOR.set(auditor);
    }

    public static Optional<Long> getCurrentAuditor() {
        return Optional.ofNullable(CURRENT_AUDITOR.get());
    }

    public static void clearAuditor() {
        CURRENT_AUDITOR.remove();
    }

    @Bean
    @Primary
    public AuditorAware<Long> testAuditorAware() {
        return () -> getCurrentAuditor().or(() -> Optional.of(1L));
    }
}
