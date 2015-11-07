package io.yope.payment.repository;

import io.yope.payment.domain.YopeUser;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author massi
 *
 */
@AllArgsConstructor
public class RedisUserRepository implements UserRepository {

    Map<String, YopeUser> users;

    public RedisUserRepository() {
        final Set<GrantedAuthority> authorities = getGrantedAuthorities("ADMIN");
        createUser(new User("admin@yope.io", "Vit9uZ2S", authorities));
    }

    /* (non-Javadoc)
     * @see io.yope.payment.security.repositories.UserRepository#createUser(org.springframework.security.core.userdetails.User)
     */
    @Override
    public User createUser(final User user) {
        final Collection<String> authorities = getAuthorities(user);
        users.put(user.getUsername(),
                YopeUser.builder()
                        .roles(authorities)
                        .password(user.getPassword())
                        .username(user.getUsername()).build());
        return user;
    }

    /* (non-Javadoc)
     * @see io.yope.payment.security.repositories.UserRepository#getUser(java.lang.String)
     */
    @Override
    public User getUser(final String username) {
        final YopeUser yopeUser = users.get(username);
        if (yopeUser != null) {
            return getUser(yopeUser);
        }
        return null;
    }


    private User getUser(final YopeUser user) {
        return new User(user.getUsername(), user.getPassword(),
                getGrantedAuthorities(user.getRoles().iterator().next()) );
    }

    private Set<GrantedAuthority> getGrantedAuthorities(final String role) {
        final Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(role));
        return authorities;
    }

    private Collection<String> getAuthorities(final User user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    @Override
    public User deleteUser(final String username) {
        return getUser(users.remove(username));
    }

}
