/**
 *
 */
package io.yope.payment.security.repositories;

import org.springframework.security.core.userdetails.User;

/**
 * @author massi
 *
 */
public interface UserRepository {

    User createUser(User user);

    User getUser(String username);

    Boolean deleteUser(String username);
}
