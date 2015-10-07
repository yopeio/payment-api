/**
 *
 */
package io.yope.payment.services;

import org.springframework.security.core.userdetails.User;

/**
 * @author mgerardi
 *
 */
public interface SecurityService {

    boolean createCredentials(String username, String password);

    User getUser();
}
