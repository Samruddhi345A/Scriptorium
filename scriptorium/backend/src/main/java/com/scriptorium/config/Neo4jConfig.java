package com.scriptorium.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Neo4j is auto-configured by Spring Boot via application.properties.
 * This class enables Neo4j repositories explicitly and keeps the door open
 * for custom driver / transaction manager beans if needed later.
 */
@Configuration
@EnableNeo4jRepositories(basePackages = "com.scriptorium.repository")
@EnableTransactionManagement
public class Neo4jConfig {
    // Spring Boot auto-configures the Neo4jClient, Neo4jTemplate, and
    // ReactiveNeo4jTransactionManager from spring.neo4j.* properties.
    // No additional beans are required for the current setup.
}
