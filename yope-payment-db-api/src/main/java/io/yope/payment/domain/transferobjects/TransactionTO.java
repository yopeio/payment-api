/**
 *
 */
package io.yope.payment.domain.transferobjects;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

    private Wallet source;

    private Wallet destination;

    private String reference;

    private Status status;

    private String description;

    private BigDecimal amount;

    private Long creationDate;

    private Long acceptedDate;

    private Long deniedDate;

    private Long completedDate;

}
