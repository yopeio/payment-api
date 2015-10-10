package io.yope.payment.rest.resources;

import java.text.MessageFormat;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Account.Type;
import io.yope.payment.domain.transferobjects.AccountTO;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.rest.requests.RegistrationRequest;

/**
 * Account resource.
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/accounts")
public class AccountResource extends BaseResource {

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
        final AccountTO to = accountHelper.registerAccount(registrationRequest);
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
    @RolesAllowed("ADMIN")
    public @ResponseBody PaymentResponse<Account> updateAccount(final HttpServletResponse response,
            @PathVariable("accountId") final Long accountId,
            @RequestBody(required=false) final AccountTO account) {
        final Account loggedAccount = getLoggedAccount();
        if (!Type.ADMIN.equals(loggedAccount.getType())) {
            return this.unauthorized(null);
        }
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.ACCEPTED.getStatusCode());
        try {
            final AccountTO updated = accountHelper.update(accountId, account);
            return new PaymentResponse<Account>(header, updated);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(null, e.getMessage());
        }

    }

    /**
     * Update Personal Account.
     * @param account
     * @return
     */
    @RequestMapping(value="/me", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Account> updateAccount(final HttpServletResponse response,
                                                                @RequestBody(required=false) final AccountTO account) {
        final Account loggedAccount = getLoggedAccount();
        if (loggedAccount == null) {
            return this.unauthorized(null);
        }
        return this.updateAccount(response, loggedAccount.getId(), account);
    }

    /**
     * Get Account By ID.
     * @param accountId
     * @return
     */
    @RequestMapping(value="/{accountId}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PaymentResponse<Account> getAccount(@PathVariable("accountId") final Long accountId) {
        final Account loggedAccount = getLoggedAccount();
        if (!Type.ADMIN.equals(loggedAccount.getType())) {
            return this.unauthorized(null);
        }
        final Account account = accountHelper.getById(accountId);
        if (account == null) {
            return this.notFound(null, MessageFormat.format("Account {0} not found", accountId));
        }
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());
        return new PaymentResponse<Account>(header, account);
    }

    /**
     * Get Personal Account.
     * @return
     */
    @RequestMapping(value="/me", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PaymentResponse<Account> getAccount() {
        final Account loggedAccount = getLoggedAccount();
        if (loggedAccount == null) {
            return this.unauthorized(null);
        }
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());
        return new PaymentResponse<Account>(header, loggedAccount);
    }

    /**
     * List of existing accounts.
     * @param accountId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PaymentResponse<List<AccountTO>> getAccounts() {
        final List<AccountTO> accounts = accountHelper.getAccounts();
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());

        final Account loggedAccount = getLoggedAccount();
        if (Type.ADMIN.equals(loggedAccount.getType())) {
            return new PaymentResponse<List<AccountTO>>(header, accounts);
        }
        return this.unauthorized(null);
    }

    /**
     * Delete Personal Account.
     * @return
     */
    @RequestMapping(value="/{accountId}", method = RequestMethod.DELETE, produces = "application/json")
    public @ResponseBody PaymentResponse<Account> deleteAccount(final HttpServletResponse response,
            @PathVariable("accountId") final Long accountId) {
        final Account loggedAccount = getLoggedAccount();
        if (!Type.ADMIN.equals(loggedAccount.getType())) {
            return this.unauthorized(null);
        }
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.ACCEPTED.getStatusCode());
        try {
            final AccountTO account = accountHelper.delete(accountId);
            response.setStatus(Response.Status.ACCEPTED.getStatusCode());
            return new PaymentResponse<Account>(header, account);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(null, e.getMessage());
        }

    }

    /**
     * Delete Personal Account.
     * @return
     */
    @RequestMapping(value="/me", method = RequestMethod.DELETE, produces = "application/json")
    public @ResponseBody PaymentResponse<Account> deleteAccount(final HttpServletResponse response) {
        final Account loggedAccount = getLoggedAccount();
        if (loggedAccount == null) {
            return this.unauthorized(null);
        }
        return this.deleteAccount(response, loggedAccount.getId());
    }


}
