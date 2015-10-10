/**
 *
 */
package io.yope.payment.security.services;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import io.yope.payment.security.repositories.UserRepository;
import io.yope.payment.services.UserSecurityService;

/**
 * @author mgerardi
 *
 */
@Service
public class OAuthUserSecurityService implements UserSecurityService {

    @Autowired
    UserRepository userRepository;

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.SecurityService#createCredentials(java.lang.String, java.lang.String)
     */
    @Override
    public User createUser(final String username, final String password, final String role) {
        final Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(role));
        return userRepository.createUser(new User(username, password, authorities));
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.SecurityService#getUser(java.lang.String)
     */
    @Override
    public User getUser(final String username) {
        return userRepository.getUser(username);
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.SecurityService#getUser()
     */
    @Override
    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
