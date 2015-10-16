/**
 *
 */
package io.yope.payment.neo4j.repositories;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import io.yope.payment.neo4j.domain.Neo4JTransaction;

/**
 * @author massi
 *
 */
public interface TransactionRepository extends GraphRepository<Neo4JTransaction> {

    /*
     * s = source
     * d = destination
     */

    @Query("MATCH (s)-[t:PAY]->(d) where id(s) = {id} AND t.reference=~{reference} AND t.status=~{status} AND t.type=~{type} return t")
    List<Neo4JTransaction> findWalletTransactionsOut(@Param("id") Long id, @Param("reference") String reference, @Param("status") String status, @Param("type") String type);

    @Query("MATCH (s)-[t:PAY]->(d) where id(d) = {id} AND t.reference=~{reference} AND t.status=~{status} AND t.type=~{type} return t")
    List<Neo4JTransaction> findWalletTransactionsIn(@Param("id") Long id, @Param("reference") String reference, @Param("status") String status, @Param("type") String type);

    @Query("MATCH (s)-[t:PAY]-(d) where id(s) = {id} AND t.reference=~{reference} AND t.status=~{status} AND t.type=~{type} return t")
    List<Neo4JTransaction> findWalletTransactions(@Param("id") Long id, @Param("reference") String reference, @Param("status") String status, @Param("type") String type);

    @Query("MATCH (a)-[:OWN]->(s)<-[t:PAY]-(d) where id(a) = {accountId} AND t.reference=~{reference} AND t.status=~{status} AND t.type=~{type} return t")
    List<Neo4JTransaction> findAccountTransactionsIn(@Param("accountId") Long accountId, @Param("reference") String reference, @Param("status") String status, @Param("type") String type);

    @Query("MATCH (a)-[:OWN]->(s)-[t:PAY]->(d) where id(a) = {accountId} AND t.reference=~{reference} AND t.status=~{status} AND t.type=~{type} return t")
    List<Neo4JTransaction> findAccountTransactionsOut(@Param("accountId") Long accountId, @Param("reference") String reference, @Param("status") String status, @Param("type") String type);

    @Query("MATCH (a)-[:OWN]->(s)-[t:PAY]-(d) where id(a) = {accountId} AND t.reference=~{reference} AND t.status=~{status} AND t.type=~{type} return t")
    List<Neo4JTransaction> findAccountTransactions(@Param("accountId") Long accountId, @Param("reference") String reference, @Param("status") String status, @Param("type") String type);

    Neo4JTransaction findBySenderHash(@Param("0") String hash);

    @Query("MATCH (a)-[t:PAY {status: {status}}]->(b) where timestamp() - t.creationDate > {delay} return t ")
    List<Neo4JTransaction> findOlderThan(@Param("delay") int delay, @Param("status") String status);

}
