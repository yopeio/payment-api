/**
 *
 */
package io.yope.payment.exceptions;

/**
 * @author massi
 *
 */
public class ObjectNotFoundException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 2674596601501839136L;

    private Class<?> type;

    public ObjectNotFoundException(final String message, final Throwable cause, final Class<?> type) {
        super(message, cause);
    }

    public ObjectNotFoundException(final Throwable cause, final Class<?> type) {
        super(cause);
    }

    public ObjectNotFoundException(final String message, final Class<?> type) {
        super(message);
        this.type = type;
    }

    public ObjectNotFoundException(final Class<?> type) {
        super();
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }
}
