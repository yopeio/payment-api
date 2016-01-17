/**
 *
 */
package io.yope.payment.exceptions;

/**
 * @author massi
 *
 */
public class AuthorizationException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -3231931135458411810L;

    /**
     *
     */
    public AuthorizationException() {
    }

    /**
     * @param message
     */
    public AuthorizationException(final String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public AuthorizationException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public AuthorizationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
