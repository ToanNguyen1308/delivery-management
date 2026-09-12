package com.viettel.delivery.config;

import com.viettel.delivery.constant.AppConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Tu dong ghi nhan nguoi tao / nguoi sua cho moi entity ke thua BaseEntity.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || AppConstants.ANONYMOUS_USER.equals(authentication.getPrincipal())) {
                return Optional.of(AppConstants.SYSTEM_AUDITOR);
            }
            return Optional.ofNullable(authentication.getName()).or(() -> Optional.of(AppConstants.SYSTEM_AUDITOR));
        };
    }
}
