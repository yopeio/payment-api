/**
 *
 */
package io.yope.payment.exceptions;

import java.text.MessageFormat;

/**
 * @author massi
 *
 */
public class ObjectNotFoundException extends Exception {

    private static final String ACCOUNT_WITH_ID_NOT_FOUND = "Object {1} with id {0} Not Found";

    /**
     *
     */
    private static final long serialVersionUID = 2674596601501839136L;

    public ObjectNotFoundException(final Long id, final Class object) {
        super(MessageFormat.format(ACCOUNT_WITH_ID_NOT_FOUND, id, object.getName()));
    }


}
