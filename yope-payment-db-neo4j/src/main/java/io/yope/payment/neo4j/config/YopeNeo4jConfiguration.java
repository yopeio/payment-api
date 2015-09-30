package io.yope.payment.neo4j.config;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.core.TypeRepresentationStrategy;
import org.springframework.data.neo4j.rest.SpringCypherRestGraphDatabase;
import org.springframework.data.neo4j.support.typerepresentation.NoopRelationshipTypeRepresentationStrategy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@Configuration
@EnableScheduling
@EnableAutoConfiguration
@ComponentScan(basePackages = {"io.yope.payment.neo4j.services"})
@EnableNeo4jRepositories(basePackages = "io.yope.payment.neo4j.repositories")
@Import(RestDataConfiguration.class)
public class YopeNeo4jConfiguration extends Neo4jConfiguration {

    public YopeNeo4jConfiguration() {
        setBasePackage("io.yope.payment.neo4j.domain");
    }

    @Bean
    public GraphDatabaseService graphDatabaseService(
            @Value("neo4jUrl") final String url,
            @Value("neo4jUsername") final String username,
            @Value("neo4jPassword") final String password) {
        return new SpringCypherRestGraphDatabase(url, username, password);
    }

    @Override
    public TypeRepresentationStrategy<Relationship> relationshipTypeRepresentationStrategy() throws Exception {
        return new NoopRelationshipTypeRepresentationStrategy();
    }

}