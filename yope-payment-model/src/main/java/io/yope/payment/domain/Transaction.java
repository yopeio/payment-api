/**
 *
 */
package io.yope.payment.domain;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

/**
 * @author mgerardi
 *
 */
@Builder(builderClassName="Builder", toBuilder=true)
@Wither
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
@ToString(of = {"type", "reference", "amount", "balance", "fees", "status"}, includeFieldNames = false)
public class Transaction {

    /**
     * the status of the transaction.
     * @author mgerardi
     *
     */
    public enum Status {
        /**
         * transaction waiting to be processed.
         */
        PENDING,
        /**
         * transaction failed.
         */
        FAILED,
        /**
         * transaction expired as not completed after a given time interval.
         */
        EXPIRED,
        /**
         * transaction rejected either by external or by internal blockchain.
         */
        DENIED,
        /**
         * transaction accepted into the queue but waiting to be processed by external blockchain.
         */
        ACCEPTED,
        /**
         * transaction completed; amount has been passed from a wallet to another.
         */
        COMPLETED
    }

    public enum Direction {
        BOTH,
        OUT,
        IN
    }

    public enum Type {
        DEPOSIT,
        WITHDRAW,
        TRANSFER
    }

    private Long id;

    private String transactionHash;

    private String senderHash;

    private String receiverHash;

    private Wallet source;

    private Wallet destination;

    private Type type;

    private String reference;

    private Status status;

    private String description;

    private BigDecimal amount;

    private BigDecimal balance;

    private BigDecimal blockchainFees;

    private BigDecimal fees;

    private Long creationDate;

    private Long acceptedDate;

    private Long failedDate;

    private Long deniedDate;

    private Long expiredDate;

    private Long completedDate;

    private String QR;

}
