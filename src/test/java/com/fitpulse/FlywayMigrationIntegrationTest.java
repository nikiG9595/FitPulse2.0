package com.fitpulse;

import com.fitpulse.repository.ProgramTemplateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flyway-check;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=true",
        "spring.flyway.baseline-on-migrate=true",
        "spring.flyway.baseline-version=0",
        "spring.flyway.target=3",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.properties.hibernate.type.preferred_uuid_jdbc_type=BINARY"
})
class FlywayMigrationIntegrationTest {

    @Autowired
    private ProgramTemplateRepository templates;

    @Test
    void migrationsBuildValidatedSchemaAndSeedLibrary() {
        assertEquals(15, templates.count());
    }
}
