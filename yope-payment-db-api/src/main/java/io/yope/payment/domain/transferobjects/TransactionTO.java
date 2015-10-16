/**
 *
 */
package io.yope.payment.domain.transferobjects;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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
@Builder(builderClassName = "Builder")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
@ToString(of = {"type", "reference", "amount"}, includeFieldNames = false)
public class TransactionTO implements Transaction {

    private Long id;

    private String hash;

    private String senderHash;

    private WalletTO source;

    private WalletTO destination;

    private Type type;

    private String reference;

    private Status status;

    private String description;

    private BigDecimal amount;

    private Long creationDate;

    private Long acceptedDate;

    private Long deniedDate;

    private Long expiredDate;

    private Long completedDate;

    private String QR;

    public static TransactionTO.Builder from(final Transaction transaction) {
        return TransactionTO.builder()
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .creationDate(transaction.getCreationDate())
                .description(transaction.getDescription())
                .destination(WalletTO.from(transaction.getDestination()).availableBalance(null).balance(null).build())
                .source(WalletTO.from(transaction.getSource()).availableBalance(null).balance(null).build())
                .status(transaction.getStatus())
                .reference(transaction.getReference())
                .id(transaction.getId())
                .QR(transaction.getQR())
                .hash(transaction.getHash())
                .senderHash(transaction.getSenderHash())
                .acceptedDate(transaction.getAcceptedDate())
                .deniedDate(transaction.getDeniedDate())
                .expiredDate(transaction.getExpiredDate())
                .completedDate(transaction.getCompletedDate());
    }


}
