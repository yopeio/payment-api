/**
 *
 */
package io.yope.payment.exceptions;

import lombok.Getter;

/**
 * @author massi
 *
 */
@Getter
public class BadRequestException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 2988588413935506115L;

    private String field;

    /**
     * @param message
     */
    public BadRequestException(final String message, final String field) {
        super(message);
        this.field = field;
    }

    public BadRequestException(final String message) {
        super(message);
    }

}
