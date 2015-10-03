package io.yope.repository.user;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceNoSqlImpl implements UserService {
	
	//@Autowired private BCryptPasswordEncoder encoder;

	@Override
	public UserDetails loadUserByUsername(final String username)
			throws UsernameNotFoundException {
		
		final Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority("USER"));
		
		final User usero = new User(username, "Ronfo000", authorities);
		
		Authentication authentication= 
				new UsernamePasswordAuthenticationToken(usero, null, usero.getAuthorities()) ; 
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		return usero;
	}
}
