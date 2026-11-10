package com.fitpulse.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/** Runs after config loading, before the datasource or Flyway can connect. */
public class DatabaseTargetGuard implements EnvironmentPostProcessor, Ordered {
    @Override public int getOrder() { return Ordered.LOWEST_PRECEDENCE; }

    @Override public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty("spring.datasource.url", "");
        if (url.startsWith("jdbc:mysql:") &&
                !url.matches("jdbc:mysql://[^/]+/fitpulse2(?:\\?[^#]*)?")) {
            throw new IllegalStateException("FitPulse 2.0 изисква MySQL база fitpulse2. Свързването е отказано преди изпълнение на миграции.");
        }
    }
}
