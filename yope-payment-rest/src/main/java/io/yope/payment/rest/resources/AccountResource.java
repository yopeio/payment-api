package io.yope.payment.rest.resources;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.transferobjects.AccountTO;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.rest.helpers.AccountHelper;
import io.yope.payment.rest.requests.RegistrationRequest;
import io.yope.payment.services.SecurityService;
import lombok.extern.slf4j.Slf4j;

/**
 * Account resource.
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/accounts")
@Slf4j
public class AccountResource {

    @Autowired
    private AccountHelper accountService;

    @Autowired
    private SecurityService securityService;

    /**
     * Create Account.
     * @param response
     * @param registrationRequest
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Account> createAccount(
            final HttpServletResponse response,
            @RequestBody(required=true) final RegistrationRequest registrationRequest) {
        final AccountTO to = this.accountService.registerAccount(registrationRequest);
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());
        response.setStatus(Response.Status.CREATED.getStatusCode());
        return new PaymentResponse<Account>(header, to);
    }

    /**
     * Update Account.
      * @param accountId
     * @param account
     * @return
     */
    @RequestMapping(value="/{accountId}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Account> updateAccount(final HttpServletResponse response,
                                                                @PathVariable final Long accountId,
                                                                @RequestBody(required=false) final AccountTO account) {

        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.ACCEPTED.getStatusCode());

        final Account loggedAccount = this.getLoggedAccount();
        /*
        if (!loggedAccount.getId().equals(accountId)) {
            return new PaymentResponse<Account>(header.success(false).status(Response.Status.UNAUTHORIZED.getStatusCode()), null);
        }
        */
        try {
            final AccountTO updated = this.accountService.update(accountId, account);
            return new PaymentResponse<Account>(header, updated);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return new PaymentResponse<Account>(header.success(false).status(Response.Status.BAD_REQUEST.getStatusCode()).errorCode(e.getMessage()), null);
        }
    }

    private Account getLoggedAccount() {
        final User user = this.securityService.getUser();
        log.info("logged as {}", user.getUsername());
        return this.accountService.getByEmail(user.getUsername());
    }

    /**
     * Get Account By ID.
     * @param accountId
     * @return
     */
    @RequestMapping(value="/{accountId}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PaymentResponse<Account> getAccount(@PathVariable final Long accountId) {
        final AccountTO account = this.accountService.getById(accountId);
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());
        if (account == null) {
            header.success(false).status(Response.Status.NOT_FOUND.getStatusCode());
        }
        return new PaymentResponse<Account>(header, account);
    }

    /**
     * List of existing accounts.
     * @param accountId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PaymentResponse<List<AccountTO>> getAccounts() {
        final List<AccountTO> accounts = this.accountService.getAccounts();
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());

        final Account loggedAccount = this.getLoggedAccount();
        /*
        if (loggedAccount.getType().equals(Type.SELLER)
                || loggedAccount.getType().equals(Type.BUYER)) {
            return new PaymentResponse<List<AccountTO>>(header.success(false).status(Response.Status.UNAUTHORIZED.getStatusCode()), null);
        }
        */
        return new PaymentResponse<List<AccountTO>>(header, accounts);
    }

    /**
     * Delete Account.
     * @param accountId
     * @return
     */
    @RequestMapping(value="/{accountId}", method = RequestMethod.DELETE, produces = "application/json")
    public @ResponseBody PaymentResponse<Account> deleteAccount(final HttpServletResponse response,
                                                                @PathVariable final long accountId) {
        AccountTO account = null;
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.ACCEPTED.getStatusCode());
        try {
            account = this.accountService.delete(accountId);
            response.setStatus(Response.Status.ACCEPTED.getStatusCode());
            return new PaymentResponse<Account>(header, account);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return new PaymentResponse<Account>(header.success(false).status(Response.Status.BAD_REQUEST.getStatusCode()).errorCode(e.getMessage()), account);
        }
    }


}
