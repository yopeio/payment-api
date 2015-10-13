/**
 *
 */
package io.yope.payment.security.repositories;

import com.google.common.collect.Lists;
import io.yope.oauth.model.YopeUser;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.*;
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
        Collection<String> authorities = getAuthorities(user);
        users.put(user.getUsername(),
                YopeUser.builder()
                        .authorities(authorities)
                        .pwd(user.getPassword())
                        .user(user.getUsername()).build());
        return user;
    }

    /* (non-Javadoc)
     * @see io.yope.payment.security.repositories.UserRepository#getUser(java.lang.String)
     */
    @Override
    public User getUser(final String username) {
        YopeUser yopeUser = users.get(username);
        if (yopeUser != null) {
            return getUser(yopeUser);
        }
        return null;
    }


    private User getUser(final YopeUser user) {
        return new User(user.getUser(), user.getPwd(),
                getGrantedAuthorities(user.getAuthorities().iterator().next()) );
    }

    private Set<GrantedAuthority> getGrantedAuthorities(String role) {
        final Set<GrantedAuthority> authorities = new HashSet();
        authorities.add(new SimpleGrantedAuthority(role));
        return authorities;
    }

    private Collection<String> getAuthorities(User user) {
        Collection<String> authorities = Lists.newArrayList();
        authorities.addAll(
                user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        return authorities;
    }

}
