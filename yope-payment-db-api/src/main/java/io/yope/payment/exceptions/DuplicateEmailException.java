/**
 *
 */
package io.yope.payment.exceptions;

/**
 * @author massi
 *
 */
public class DuplicateEmailException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -1812735225872176434L;

    public String email;

    public DuplicateEmailException(final String email) {
        super(email);
        this.email = email;
    }

    public DuplicateEmailException(final String message, final String email) {
        super(message);
        this.email = email;
    }

}
