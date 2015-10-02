/**
 *
 */
package io.yope.payment.domain.transferobjects;

import java.math.BigDecimal;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;
import lombok.Builder;
import lombok.Getter;

/**
 * @author mgerardi
 *
 */
@Builder
@Getter
public class TransactionTO implements Transaction {

    private final Long id;

    private final Wallet source;

    private final Wallet destination;

    private final String reference;

    private final Status status;

    private final String description;

    private final BigDecimal amount;

    private final Long creationDate;

    private final Long acceptedDate;

    private final Long deniedDate;

    private final Long completedDate;

}
