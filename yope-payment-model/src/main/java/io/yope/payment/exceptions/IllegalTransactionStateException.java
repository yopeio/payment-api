package io.yope.payment.exceptions;

import io.yope.payment.domain.Transaction;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent=true)
public class IllegalTransactionStateException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -204342915703128442L;

    private Transaction.Status from;

    private Transaction.Status to;

}
