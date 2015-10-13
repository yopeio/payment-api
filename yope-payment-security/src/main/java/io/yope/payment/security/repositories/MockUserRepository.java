/**
 *
 */
package io.yope.payment.security.repositories;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

/**
 * @author massi
 *
 */

/**
 * Substituted by RedisUserRepository.
 */
//@Service
@Deprecated
public class MockUserRepository implements UserRepository {

    Map<String, User> users = Maps.newHashMap();

    public MockUserRepository() {
        final Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ADMIN"));
        createUser(new User("admin@yope.io", "Vit9uZ2S", authorities));
    }


    /* (non-Javadoc)
     * @see io.yope.payment.security.repositories.UserRepository#createUser(org.springframework.security.core.userdetails.User)
     */
    @Override
    public User createUser(final User user) {
        return users.put(user.getUsername(), user);
    }

    /* (non-Javadoc)
     * @see io.yope.payment.security.repositories.UserRepository#getUser(java.lang.String)
     */
    @Override
    public User getUser(final String username) {
        return users.get(username);
    }

}
