package com.fitpulse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("integration-test")
class FitPulseApplicationContextTest {
    @Test
    void applicationContextStartsWithEmbeddedDatabase() {
    }
}
