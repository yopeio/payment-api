/**
 *
 */
package io.yope.payment.domain.transferobjects;

import java.math.BigDecimal;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author mgerardi
 *
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionTO implements Transaction {

    private Long id;

    private WalletTO source;

    private WalletTO destination;

    private String reference;

    private Status status;

    private String description;

    private BigDecimal amount;

    private Long creationDate;

    private Long acceptedDate;

    private Long deniedDate;

    private Long completedDate;

    @Override
    public Type getType() {
        if (Wallet.Type.INTERNAL.equals(source.getType()) &&
            Wallet.Type.INTERNAL.equals(destination.getType())) {
            return Type.INTERNAL;
        }
        return Type.EXTERNAL;
    }

    public static TransactionTO.TransactionTOBuilder from(final Transaction transaction) {
        return TransactionTO.builder()
                .amount(transaction.getAmount())
                .creationDate(transaction.getCreationDate())
                .description(transaction.getDescription())
                .destination(WalletTO.from(transaction.getDestination()).build())
                .source(WalletTO.from(transaction.getSource()).build())
                .status(transaction.getStatus())
                .reference(transaction.getReference())
                .id(transaction.getId())
                .acceptedDate(transaction.getAcceptedDate())
                .deniedDate(transaction.getDeniedDate())
                .completedDate(transaction.getCompletedDate());
    }


}
