/**
 *
 */
package io.yope.payment.neo4j.repositories;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import io.yope.payment.domain.Wallet;
import io.yope.payment.neo4j.domain.Neo4JWallet;

/**
 * @author massi
 *
 */
public interface WalletRepository extends GraphRepository<Neo4JWallet> {

    /**
     * finds all the wollet for a given account.
     * @param accountId the account's id
     * @return a list of wallet
     */
    @Query("MATCH (a)-[:OWNS]->(w:Wallet) where id(a) = {accountId} RETURN w")
    List<Wallet> findAllByOwner(Long accountId);

}
