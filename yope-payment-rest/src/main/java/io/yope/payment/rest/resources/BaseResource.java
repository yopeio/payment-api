/**
 *
 */
package io.yope.payment.rest.resources;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;

import io.yope.payment.domain.Account;
import io.yope.payment.rest.helpers.AccountHelper;
import io.yope.payment.services.TransactionService;
import io.yope.payment.services.UserSecurityService;
import io.yope.payment.services.WalletService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mgerardi
 *
 */
@Slf4j
public abstract class BaseResource {

    @Autowired
    protected AccountHelper accountHelper;

    @Autowired
    protected UserSecurityService securityService;

    @Autowired
    protected WalletService walletService;

    @Autowired
    protected TransactionService transactionService;

    protected Account getLoggedAccount() {
        final User user = this.securityService.getCurrentUser();
        final Account account = this.accountHelper.getByEmail(user.getUsername());
        log.info("logged as {}", account);
        return account;
    }

    protected <T> PaymentResponse<T> unauthorized(final T object) {
        final ResponseHeader header = new ResponseHeader(false, Response.Status.UNAUTHORIZED.toString(), Response.Status.UNAUTHORIZED.getStatusCode());
        return new PaymentResponse<T>(header, object);
    }

    protected <T> PaymentResponse<T> badRequest(final T object, final String message) {
        final ResponseHeader header = new ResponseHeader(false, message, Response.Status.BAD_REQUEST.getStatusCode());
        return new PaymentResponse<T>(header, object);
    }

    protected <T> PaymentResponse<T> notFound(final T object, final String message) {
        final ResponseHeader header = new ResponseHeader(false, message, Response.Status.NOT_FOUND.getStatusCode());
        return new PaymentResponse<T>(header, object);
    }




}
