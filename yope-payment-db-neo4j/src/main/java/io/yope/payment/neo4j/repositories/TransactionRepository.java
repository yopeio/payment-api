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

    /**
     * s = source
     * d = destination
     * @param id
     * @return
     */
    @Query("MATCH (s)-[t:PAY]->(d) where id(s) = {id} return t")
    List<Neo4JTransaction> findWalletTransactionsOut(@Param("id") Long id);

    @Query("MATCH (s)-[t:PAY]->(d) where id(d) = {id} return t")
    List<Neo4JTransaction> findWalletTransactionsIn(@Param("id") Long id);

    @Query("MATCH (s)-[t:PAY]-(d) where id(d) = {id} return t")
    List<Neo4JTransaction> findWalletTransactions(@Param("id") Long id);

    @Query("MATCH (s)-[t:PAY {reference:{reference}}]->(d) where id(s) = {id} return t")
    List<Neo4JTransaction> findWalletTransactionsOut(@Param("id") Long id, @Param("reference") String reference);

    @Query("MATCH (s)-[t:PAY {reference:{reference}}]->(d) where id(d) = {id} return t")
    List<Neo4JTransaction> findWalletTransactionsIn(@Param("id") Long id, @Param("reference") String reference);

    @Query("MATCH (s)-[t:PAY {reference:{reference}}]-(d) where id(d) = {id} return t")
    List<Neo4JTransaction> findWalletTransactions(@Param("id") Long id, @Param("reference") String reference);

    @Query("MATCH (a)-[:OWN]->(s)-[t:PAY]->(d)<-[:OWN]-(e) where id(e)={accountId} return t")
    List<Neo4JTransaction> findAccountTransactionsIn(@Param("accountId") Long accountId);

    @Query("MATCH (a)-[:OWN]->(s)-[t:PAY]->(d)<-[:OWN]-(e) where id(a)={accountId} return t")
    List<Neo4JTransaction> findAccountTransactionsOut(@Param("accountId") Long accountId);

    @Query("MATCH (a)-[:OWN]->(s)-[t:PAY]-(d) where id(a)={accountId} return t")
    List<Neo4JTransaction> findAccountTransactions(@Param("accountId") Long accountId);

    @Query("MATCH (a)-[:OWN]->(s)<-[t:PAY {reference:{reference}}]-(d) where id(a)={accountId} return t")
    List<Neo4JTransaction> findAccountTransactionsIn(@Param("accountId") Long accountId, @Param("reference") String reference);

    @Query("MATCH (a)-[:OWN]->(s)-[t:PAY {reference:{reference}}]->(d) where id(a)={accountId} return t")
    List<Neo4JTransaction> findAccountTransactionsOut(@Param("accountId") Long accountId, @Param("reference") String reference);

    @Query("MATCH (a)-[:OWN]->(s)-[t:PAY {reference:{reference}}]-(d) where id(a)={accountId} return t")
    List<Neo4JTransaction> findAccountTransactions(@Param("accountId") Long accountId, @Param("reference") String reference);

}
