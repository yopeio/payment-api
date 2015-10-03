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
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @author mgerardi
 *
 */
@Getter
@Builder
@ToString(of = {"reference", "status"}, includeFieldNames = true)
@RelationshipEntity(type="PAY")
public class Neo4JTransaction implements Transaction {

    @GraphId
    private final Long id;

    @Fetch
    @StartNode
    private final Neo4JWallet source;

    @Fetch
    @EndNode
    private final Neo4JWallet destination;

    private final String reference;

    private final Status status;

    private final String description;

    private final BigDecimal amount;

    private final Long creationDate;

    private final Long acceptedDate;

    private final Long deniedDate;

    private final Long completedDate;

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
                .acceptedDate(transaction.getAcceptedDate())
                .deniedDate(transaction.getDeniedDate())
                .completedDate(transaction.getCompletedDate());
    }

}
