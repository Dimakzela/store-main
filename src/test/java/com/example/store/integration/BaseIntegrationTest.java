package com.example.store.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers // Activates the container lifecycle hooks
public abstract class BaseIntegrationTest {

    // Automatically pulls, creates, and runs a matching PostgreSQL Docker node
    @Container
    @ServiceConnection // Crucial Spring Boot 3.1+: Automatically injects JDBC URLs, usernames, and passwords
    protected static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("store_test_db")
            .withUsername("test_admin")
            .withPassword("test_secure_pass");
}
