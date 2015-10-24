/**
 *
 */
package io.yope.payment.neo4j.repositories;

import java.util.List;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import io.yope.payment.neo4j.domain.Neo4JAccount;

/**
 * @author massi
 *
 */
public interface AccountRepository extends GraphRepository<Neo4JAccount> {

    Neo4JAccount findByEmail(@Param("0") String email);

    List<Neo4JAccount> findByType(@Param("0") String type);

}
