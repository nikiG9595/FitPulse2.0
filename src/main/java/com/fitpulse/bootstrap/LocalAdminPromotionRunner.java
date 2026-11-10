package com.fitpulse.bootstrap;

import com.fitpulse.model.entity.User;
import com.fitpulse.model.enums.UserRole;
import com.fitpulse.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Explicit, development-only bootstrap command for promoting an existing user.
 * It never creates accounts and never accepts or handles passwords.
 */
@Component
@Profile("dev")
@ConditionalOnProperty(name = "fitpulse.local-admin-promotion.confirm", havingValue = "true")
public class LocalAdminPromotionRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAdminPromotionRunner.class);

    private final UserRepository userRepository;
    private final String username;

    public LocalAdminPromotionRunner(
            UserRepository userRepository,
            @Value("${fitpulse.local-admin-promotion.username:}") String username) {
        this.userRepository = userRepository;
        this.username = username == null ? "" : username.trim();
    }

    @Override
    public void run(String... args) {
        if (username.isBlank()) {
            throw new IllegalArgumentException(
                    "Admin promotion requires --fitpulse.local-admin-promotion.username=<existing username>");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot promote user: username does not exist: " + username));

        if (user.getRole() == UserRole.ADMIN) {
            LOGGER.info("Local admin promotion skipped: userId={} is already ADMIN", user.getId());
            return;
        }

        user.setRole(UserRole.ADMIN);
        userRepository.save(user);
        LOGGER.info("Local admin promotion completed: userId={} is now ADMIN", user.getId());
    }
}
