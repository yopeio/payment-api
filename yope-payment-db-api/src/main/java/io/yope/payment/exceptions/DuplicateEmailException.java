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
public class DuplicateEmailException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -1812735225872176434L;

    public String email;

    public DuplicateEmailException(final String message) {
        super(message);
    }


}
