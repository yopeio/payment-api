/**
 *
 */
package io.yope.payment.exceptions;

/**
 * @author massi
 *
 */
public class InsufficientFundsException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -6059041713631635870L;

    /**
     * @param message
     */
    public InsufficientFundsException(final String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public InsufficientFundsException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public InsufficientFundsException(final String message, final Throwable cause) {
        super(message, cause);
    }


}
