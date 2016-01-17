/**
 *
 */
package io.yope.payment.dynamodb.domain;

import java.math.BigDecimal;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import io.yope.payment.domain.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author mgerardi
 *
 */
@Getter
@Builder(builderClassName="Builder", toBuilder=true)
@AllArgsConstructor
@NoArgsConstructor
@ToString(of = {"type", "reference", "amount", "balance", "fees", "status"}, includeFieldNames = false)
@DynamoDBTable(tableName = "Transaction")
public class DynamodbTransaction {

    private Long id;

    private Long sourceId;

    private Long destinationId;

    private String transactionHash;

    private String senderHash;

    private String receiverHash;

    private Transaction.Type type;

    private String reference;

    private Transaction.Status status;

    private String description;

    private BigDecimal amount;

    private BigDecimal balance;

    private BigDecimal blockchainFees;

    private BigDecimal fees;

    private Long creationDate;

    private Long acceptedDate;

    private Long deniedDate;

    private Long failedDate;

    private Long expiredDate;

    private Long completedDate;

    private String QR;

    public static DynamodbTransaction.Builder from(final Transaction transaction) {
        return DynamodbTransaction.builder()
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balance(transaction.getBalance())
                .blockchainFees(transaction.getBlockchainFees())
                .fees(transaction.getFees())
                .creationDate(transaction.getCreationDate())
                .description(transaction.getDescription())
                .destinationId(transaction.getDestination().getId())
                .sourceId(transaction.getSource().getId())
                .status(transaction.getStatus())
                .reference(transaction.getReference())
                .id(transaction.getId())
                .QR(transaction.getQR())
                .transactionHash(transaction.getTransactionHash())
                .senderHash(transaction.getSenderHash())
                .receiverHash(transaction.getReceiverHash())
                .acceptedDate(transaction.getAcceptedDate())
                .failedDate(transaction.getFailedDate())
                .expiredDate(transaction.getExpiredDate())
                .deniedDate(transaction.getDeniedDate())
                .completedDate(transaction.getCompletedDate());
    }

    public Transaction toTransaction() {
        return Transaction.builder()
                .type(this.getType())
                .amount(this.getAmount())
                .balance(this.getBalance())
                .blockchainFees(this.getBlockchainFees())
                .fees(this.getFees())
                .creationDate(this.getCreationDate())
                .description(this.getDescription())
                .status(this.getStatus())
                .reference(this.getReference())
                .id(this.getId())
                .QR(this.getQR())
                .transactionHash(this.getTransactionHash())
                .senderHash(this.getSenderHash())
                .receiverHash(this.getReceiverHash())
                .acceptedDate(this.getAcceptedDate())
                .failedDate(this.getFailedDate())
                .expiredDate(this.getExpiredDate())
                .deniedDate(this.getDeniedDate())
                .completedDate(this.getCompletedDate()).build();
    }
}
