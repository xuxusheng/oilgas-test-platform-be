package com.yimusi.config;

import java.util.Optional;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;

@TestConfiguration
public class TestAuditorConfig {

    private static final ThreadLocal<String> CURRENT_AUDITOR = new ThreadLocal<>();

    public static void setAuditor(String auditor) {
        CURRENT_AUDITOR.set(auditor);
    }

    public static Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(CURRENT_AUDITOR.get());
    }

    public static void clearAuditor() {
        CURRENT_AUDITOR.remove();
    }

    @Bean
    @Primary
    public AuditorAware<String> testAuditorAware() {
        return () -> getCurrentAuditor().or(() -> Optional.of("test-system"));
    }
}
