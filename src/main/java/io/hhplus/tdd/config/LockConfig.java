package io.hhplus.tdd.config;

import io.hhplus.tdd.domain.point.service.lock.UserLockManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LockConfig {

    @Bean
    public UserLockManager userLockManager() {
        return new UserLockManager(24 * 60 * 60 * 1000L);
    }
}

