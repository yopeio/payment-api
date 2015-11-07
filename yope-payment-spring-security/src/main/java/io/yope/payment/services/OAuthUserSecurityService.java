/**
 *
 */
package io.yope.payment.services;

import com.google.common.collect.Lists;
import io.yope.payment.db.services.UserSecurityService;
import io.yope.payment.domain.YopeUser;
import io.yope.payment.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

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
        YopeUser user = (YopeUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            authorities.add(new SimpleGrantedAuthority(user.getRoles().iterator().next()));
        }
        return new User(user.getUsername(), user.getPassword(), authorities);
    }

    @Override
    public User deleteUser(final String username) {
        return userRepository.deleteUser(username);
    }



}
