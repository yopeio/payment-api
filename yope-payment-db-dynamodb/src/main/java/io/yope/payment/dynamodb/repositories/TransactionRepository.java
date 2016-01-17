/**
 *
 */
package io.yope.payment.dynamodb.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.dynamodb.domain.DynamodbTransaction;

/**
 * @author mgerardi
 *
 */
public interface TransactionRepository extends CrudRepository<DynamodbTransaction, Long>{

    DynamodbTransaction findByReceiverHash(@Param("receiverHash") String hash);

    List<DynamodbTransaction> findBySourceId(@Param("sourceId") Long id);

    List<DynamodbTransaction> findByDestinationId(@Param("sourceId") Long id);

    DynamodbTransaction findByTransactionHash(@Param("transactionHash") String hash);

    List<DynamodbTransaction> findByStatus(@Param("status") Status status);

    DynamodbTransaction findBySenderHash(@Param("senderHash")  String hash);

}
