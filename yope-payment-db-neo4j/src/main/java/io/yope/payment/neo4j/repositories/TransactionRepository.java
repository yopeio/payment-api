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

    @Query("MATCH (s {hash:{hash}})-[t:PAY]->(d) return t")
    List<Neo4JTransaction> findWalletTransactionsOut(@Param("hash") String hash);

    @Query("MATCH (s)-[t:PAY]->(d {hash:{hash}}) return t")
    List<Neo4JTransaction> findWalletTransactionsIn(@Param("hash") String hash);

    @Query("MATCH (s)-[t:PAY]->(d) where s.hash = {hash} or d.hash = {hash} return t")
    List<Neo4JTransaction> findWalletTransactions(@Param("hash") String hash);

    @Query("MATCH (s {hash:{hash}})-[t:PAY {reference:{reference}}]->(d) return t")
    List<Neo4JTransaction> findWalletTransactionsOut(@Param("hash") String hash, @Param("reference") String reference);

    @Query("MATCH (s)-[t:PAY {reference:{reference}}]->(d {hash:{hash}}) return t")
    List<Neo4JTransaction> findWalletTransactionsIn(@Param("hash") String hash, @Param("reference") String reference);

    @Query("MATCH (s)-[t:PAY {reference:{reference}}]->(d) where s.hash = {hash} or d.hash = {hash} return t")
    List<Neo4JTransaction> findWalletTransactions(@Param("hash") String hash, @Param("reference") String reference);

    @Query("MATCH (a)-[:OWN]->(b)-[t:PAY]->(d)<-[:OWN]-(e)  where id(e)={accountId} return t")
    List<Neo4JTransaction> findAccountTransactionsIn(@Param("accountId") Long accountId);

    @Query("MATCH (a)-[:OWN]->(b)-[t:PAY]->(d)<-[:OWN]-(e)  where id(a)={accountId} return t")
    List<Neo4JTransaction> findAccountTransactionsOut(@Param("accountId") Long accountId);

    @Query("MATCH (a)-[:OWN]->(b)-[t:PAY]->(d)<-[:OWN]-(e)  where id(a)={accountId} or id(e)={accountId} return t")
    List<Neo4JTransaction> findAccountTransactions(@Param("accountId") Long accountId);

    @Query("MATCH (a)-[:OWN]->(b)-[t:PAY]->(d)<-[:OWN]-(e)  where id(e)={accountId} and t.reference={reference} return t")
    List<Neo4JTransaction> findAccountTransactionsIn(@Param("accountId") Long accountId, @Param("reference") String reference);

    @Query("MATCH (a)-[:OWN]->(b)-[t:PAY]->(d)<-[:OWN]-(e)  where id(a)={accountId} and t.reference={reference} return t")
    List<Neo4JTransaction> findAccountTransactionsOut(@Param("accountId") Long accountId, @Param("reference") String reference);

    @Query("MATCH (a)-[:OWN]->(b)-[t:PAY]->(d)<-[:OWN]-(e)  where id(a)={accountId} and t.reference={reference} or id(e)={accountId} return t")
    List<Neo4JTransaction> findAccountTransactions(@Param("accountId") Long accountId, @Param("reference") String reference);

}
