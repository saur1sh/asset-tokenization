package com.rwa.common.test;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@org.springframework.boot.test.context.SpringBootTest
public abstract class BaseIntegrationTest {

    protected static PostgreSQLContainer<?> postgres;
    protected static KafkaContainer kafka;

    static {
        try {
            postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"));
            kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));
            postgres.start();
            kafka.start();
        } catch (Exception e) {
            System.err.println("Docker not available, falling back to H2 and Mock Kafka. Error: " + e.getMessage());
            postgres = null;
            kafka = null;
        }
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        if (postgres != null && postgres.isRunning()) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
        } else {
            registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
            registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
            registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
        }

        if (kafka != null && kafka.isRunning()) {
            registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        } else {
            // If No Docker, we'll rely on @EmbeddedKafka in subclasses if needed, 
            // or just point to a dummy server for context loads.
            registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
        }
    }
}
