/**
 *
 */
package io.yope.payment.neo4j.config;

import org.springframework.boot.autoconfigure.data.rest.SpringBootRepositoryRestMvcConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;

import io.yope.payment.neo4j.domain.Neo4JAccount;
import io.yope.payment.neo4j.domain.Neo4JTransaction;
import io.yope.payment.neo4j.domain.Neo4JWallet;

/**
 * @author massi
 *
 */
@Configuration
public class RestDataConfiguration extends SpringBootRepositoryRestMvcConfiguration {

    @Override
    protected void configureRepositoryRestConfiguration(final RepositoryRestConfiguration config) {
        super.configureRepositoryRestConfiguration(config);
        config.exposeIdsFor(Neo4JWallet.class, Neo4JAccount.class, Neo4JTransaction.class);
    }

}
