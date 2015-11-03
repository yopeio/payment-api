package io.yope.repository.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import io.yope.payment.db.services.UserSecurityService;

@Service
public class UserServiceNoSqlImpl implements UserService {

	@Autowired private BCryptPasswordEncoder encoder;
    @Autowired private UserSecurityService service;

	@Override
	public UserDetails loadUserByUsername(final String username)
			throws UsernameNotFoundException {

		final User user = service.getUser(username);
		
		if (null == user) {
            throw new UsernameNotFoundException("The user with email " + username + " was not found");
        }

		final Authentication authentication=
				new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()) ;
		SecurityContextHolder.getContext().setAuthentication(authentication);

		return user;
	}
}
