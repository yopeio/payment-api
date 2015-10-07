/**
 *
 */
package io.yope.payment.security.services;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import io.yope.payment.services.SecurityService;

/**
 * @author mgerardi
 *
 */
@Service
public class OAuthSecurityService implements SecurityService {

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.SecurityService#createCredentials(java.lang.String, java.lang.String)
     */
    @Override
    public boolean createCredentials(final String username, final String password) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.SecurityService#getUser()
     */
    @Override
    public User getUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
