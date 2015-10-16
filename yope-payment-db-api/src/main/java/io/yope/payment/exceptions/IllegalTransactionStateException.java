package io.yope.payment.exceptions;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Status;
import lombok.Getter;

@Getter
public class IllegalTransactionStateException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -204342915703128442L;

    Transaction.Status from;

    Transaction.Status to;

    public IllegalTransactionStateException(final Status from, final Status to) {
        super("Illegal Transaction State from " + from + " to " +to);
        this.from = from;
        this.to = to;
    }




}
