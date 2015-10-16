package io.yope.payment.exceptions;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Type;
import lombok.Getter;

@Getter
public class IllegalTransactionException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -3656311788668404871L;

    private static final String MESSAGE = "Wrong Transaction; expected {0}, found {1}: {3}";

    Transaction.Type type;

    Transaction.Type expected;

    public IllegalTransactionException(final Type type, final Transaction.Type expected,final String message) {
        super(MessageFormat.format(MESSAGE, type, expected, StringUtils.defaultIfBlank(message, "")));
        this.type = type;
        this.expected = expected;
    }


}
