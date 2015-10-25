/**
 *
 */
package io.yope.payment.exceptions;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author massi
 *
 */
@Getter
@Setter
@Accessors(fluent=true)
public class BadRequestException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 2988588413935506115L;

    private String field;

    /**
     * @param message
     */
    public BadRequestException(final String message) {
        super(message);
    }

}
