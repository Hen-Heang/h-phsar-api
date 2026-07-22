package com.henheang.hphsar;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared base for MyBatis/service integration tests. Boots a real, disposable
 * PostgreSQL container per test class (never a production, shared, or
 * developer database) and applies the project's own schema.sql so tests run
 * against the real table/column shapes. {@code @ServiceConnection} wires the
 * container's JDBC URL/credentials into Spring automatically.
 * <p>
 * The "test" profile (application-test.yaml) supplies safe non-DB values
 * (mail host, JWT secret) so the context can start without any real secret.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withInitScript("script/schema.sql");
}
