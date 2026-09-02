package com.henheang.hphsar;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared base for MyBatis/service integration tests. Boots one real,
 * disposable PostgreSQL container for the whole JVM/Failsafe fork (never a
 * production, shared, or developer database) and applies the project's own
 * schema.sql so tests run against the real table/column shapes.
 * {@code @ServiceConnection} wires the container's JDBC URL/credentials into
 * Spring automatically.
 * <p>
 * The container is started manually in a static initializer instead of being
 * annotated with Testcontainers' {@code @Container}/{@code @Testcontainers}.
 * Those annotations stop the container in {@code afterAll} of every subclass
 * that extends this base, so a shared static field would be torn down after
 * the first {@code *IT} class finished, leaving later classes' cached Spring
 * contexts pointing at a stopped container. This singleton-container pattern
 * keeps one container alive for every {@code *IT} class; only the
 * Testcontainers Ryuk reaper stops it, at JVM exit.
 * <p>
 * The "test" profile (application-test.yaml) supplies safe non-DB values
 * (mail host, JWT secret) so the context can start without any real secret.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withInitScript("script/schema.sql");

    static {
        POSTGRES.start();
    }
}
