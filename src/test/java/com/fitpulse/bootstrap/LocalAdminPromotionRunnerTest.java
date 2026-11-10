package com.fitpulse.bootstrap;

import com.fitpulse.model.entity.User;
import com.fitpulse.model.enums.UserRole;
import com.fitpulse.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalAdminPromotionRunnerTest {

    private final UserRepository repository = mock(UserRepository.class);

    @Test
    void promotesExistingMemberWithoutChangingCredentials() {
        User user = new User();
        user.setUsername("owner");
        user.setPassword("existing-hash");
        user.setRole(UserRole.MEMBER);
        when(repository.findByUsername("owner")).thenReturn(Optional.of(user));

        new LocalAdminPromotionRunner(repository, " owner ").run();

        assertEquals(UserRole.ADMIN, user.getRole());
        assertEquals("existing-hash", user.getPassword());
        verify(repository).save(user);
    }

    @Test
    void isIdempotentForExistingAdmin() {
        User user = new User();
        user.setRole(UserRole.ADMIN);
        when(repository.findByUsername("owner")).thenReturn(Optional.of(user));

        new LocalAdminPromotionRunner(repository, "owner").run();

        verify(repository, never()).save(user);
    }

    @Test
    void rejectsMissingUsernameOrUnknownUser() {
        assertThrows(IllegalArgumentException.class,
                () -> new LocalAdminPromotionRunner(repository, " ").run());
        when(repository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> new LocalAdminPromotionRunner(repository, "missing").run());
    }
}
