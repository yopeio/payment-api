package io.yope.payment.services;

import io.yope.payment.db.services.UserSecurityService;
import io.yope.payment.domain.YopeUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

public class UserServiceAuthenticator implements ExternalServiceAuthenticator {

    @Autowired
    private UserSecurityService userSecurityService;

    @Override
    public AuthenticatedExternalWebService authenticate(String username, String password) {
        ExternalWebServiceStub externalWebService = new ExternalWebServiceStub();


        // Do all authentication mechanisms required by external web service protocol and validated response.
        // Throw descendant of Spring AuthenticationException in case of unsucessful authentication. For example BadCredentialsException

        User user = userSecurityService.getUser(username);
        if(user == null || !password.equals(user.getPassword())) {
            throw new BadCredentialsException("user " + username + " not found");
        }

        // If authentication to external service succeeded then create authenticated wrapper with proper Principal and GrantedAuthorities.
        // GrantedAuthorities may come from external service authentication or be hardcoded at our layer as they are here with ROLE_DOMAIN_USER
        AuthenticatedExternalWebService authenticatedExternalWebService =
                new AuthenticatedExternalWebService(
                        YopeUser.builder().username(username).password(password).build(), null,
                AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_DOMAIN_USER"));
        authenticatedExternalWebService.setExternalWebService(externalWebService);

        return authenticatedExternalWebService;
    }
}
