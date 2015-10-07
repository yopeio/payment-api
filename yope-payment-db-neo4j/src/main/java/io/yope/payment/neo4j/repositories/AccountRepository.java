/**
 *
 */
package io.yope.payment.neo4j.repositories;

import org.springframework.data.neo4j.repository.GraphRepository;

import io.yope.payment.neo4j.domain.Neo4JAccount;

/**
 * @author massi
 *
 */
public interface AccountRepository extends GraphRepository<Neo4JAccount> {

    Neo4JAccount findByEmail(String email);

}
