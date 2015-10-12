package io.yope.repository.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.yope.payment.services.UserSecurityService;

@Service
public class UserServiceNoSqlImpl implements UserService {

	//@Autowired private BCryptPasswordEncoder encoder;

    @Autowired
    UserSecurityService service;

	@Override
	public UserDetails loadUserByUsername(final String username)
			throws UsernameNotFoundException {

		final User usero = service.getUser(username);

		final Authentication authentication=
				new UsernamePasswordAuthenticationToken(usero, null, usero.getAuthorities()) ;
		SecurityContextHolder.getContext().setAuthentication(authentication);

		return usero;
	}
}
