/**
 *
 */
package io.yope.payment.neo4j.repositories;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import io.yope.payment.neo4j.domain.Neo4JWallet;

/**
 * @author massi
 *
 */
public interface WalletRepository extends GraphRepository<Neo4JWallet> {

    @Query("MATCH (a)-[:OWN]->(w) where id(a) = {accountId} AND w.status=~{status} RETURN w")
    List<Neo4JWallet> findAllByOwner(@Param("accountId") Long accountId, @Param("status") String status);

    @Query("MATCH (a)-[:OWN]->(w {name: {name}}) where id(a) = {accountId} RETURN w")
    Neo4JWallet findByName(@Param("accountId") Long accountId, @Param("name") String name);

    Neo4JWallet findByWalletHash(@Param("0") String hash);

}
