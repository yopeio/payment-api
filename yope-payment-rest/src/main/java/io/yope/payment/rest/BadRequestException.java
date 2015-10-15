/**
 *
 */
package io.yope.payment.rest;

/**
 * @author massi
 *
 */
public class BadRequestException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 6869623613558204056L;

    /**
     *
     */
    public BadRequestException() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public BadRequestException(final String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public BadRequestException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public BadRequestException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
