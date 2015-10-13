/**
 *
 */
package io.yope.payment.neo4j.domain;

import java.math.BigDecimal;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(of = {"reference", "status"}, includeFieldNames = true)
@RelationshipEntity(type="PAY")
public class Neo4JTransaction implements Transaction {

    @GraphId
    private Long id;

    @Fetch
    @StartNode
    private Neo4JWallet source;

    @Fetch
    @EndNode
    private Neo4JWallet destination;

    private String hash;

    private String reference;

    private Status status;

    private String description;

    private BigDecimal amount;

    private Long creationDate;

    private Long acceptedDate;

    private Long deniedDate;

    private Long completedDate;

    public static Neo4JTransaction.Neo4JTransactionBuilder from(final Transaction transaction) {
        return Neo4JTransaction.builder()
                .amount(transaction.getAmount())
                .creationDate(transaction.getCreationDate())
                .description(transaction.getDescription())
                .destination(Neo4JWallet.from(transaction.getDestination()).build())
                .source(Neo4JWallet.from(transaction.getSource()).build())
                .status(transaction.getStatus())
                .reference(transaction.getReference())
                .id(transaction.getId())
                .hash(transaction.getHash())
                .acceptedDate(transaction.getAcceptedDate())
                .deniedDate(transaction.getDeniedDate())
                .completedDate(transaction.getCompletedDate());
    }

    @Override
    public Type getType() {
        if (Wallet.Type.INTERNAL.equals(source.getType()) &&
            Wallet.Type.INTERNAL.equals(destination.getType())) {
            return Type.INTERNAL;
        }
        return Type.EXTERNAL;
    }

}
