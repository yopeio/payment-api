/**
 *
 */
package io.yope.payment.db.services;

import org.springframework.security.core.userdetails.User;

/**
 * @author mgerardi
 *
 */
public interface UserSecurityService {

    User createUser(String username, String password, String role);

    User getUser(String username);

    User getCurrentUser();

    User deleteUser(String username);


}
