package io.yope.payment.configuration;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.core.TypeRepresentationStrategy;
import org.springframework.data.neo4j.rest.SpringRestGraphDatabase;
import org.springframework.data.neo4j.support.typerepresentation.NoopRelationshipTypeRepresentationStrategy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import lombok.Getter;
import lombok.Setter;

@EnableTransactionManagement
@Configuration
@EnableScheduling
@EnableAutoConfiguration
@EnableConfigurationProperties
@ComponentScan(basePackages = {"io.yope.payment.neo4j.services"})
@EnableNeo4jRepositories(basePackages = "io.yope.payment.neo4j.repositories")
@Import(RestDataConfiguration.class)
public class YopeNeo4jConfiguration extends Neo4jConfiguration {

    @ConfigurationProperties(prefix = "neo4j") @Getter
    @Setter
    public class Neo4jSettings {
        private String neo4jUrl;
        private String neo4jUsername;
        private String neo4jPassword;
    }

    public YopeNeo4jConfiguration() {
        setBasePackage("io.yope.payment.neo4j.domain");
    }

    @Bean
    public Neo4jSettings neo4jSettings() {
        return new Neo4jSettings();
    }

    @Bean
    public GraphDatabaseService graphDatabaseService(
            final Neo4jSettings settings) {
        return new SpringRestGraphDatabase(settings.getNeo4jUrl(), settings.getNeo4jUsername(), settings.getNeo4jPassword());
    }

    @Override
    public TypeRepresentationStrategy<Relationship> relationshipTypeRepresentationStrategy() throws Exception {
        return new NoopRelationshipTypeRepresentationStrategy();
    }

}