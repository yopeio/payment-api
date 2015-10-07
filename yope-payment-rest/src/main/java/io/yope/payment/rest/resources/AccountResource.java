package io.yope.payment.rest.resources;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
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

/**
 * Account resource.
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/accounts")
public class AccountResource {

    @Autowired
    private AccountHelper accountService;

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
        final AccountTO to = accountService.registerAccount(registrationRequest);
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

        SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.ACCEPTED.getStatusCode());
        try {
            final AccountTO updated = accountService.update(accountId, account);
            return new PaymentResponse<Account>(header, updated);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return new PaymentResponse<Account>(header.success(false).status(Response.Status.BAD_REQUEST.getStatusCode()).errorCode(e.getMessage()), account);
        }
    }

    /**
     * Get Account By ID.
     * @param accountId
     * @return
     */
    @RequestMapping(value="/{accountId}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PaymentResponse<Account> getAccount(@PathVariable final Long accountId) {
        final AccountTO account = accountService.getById(accountId);
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
        final List<AccountTO> accounts = accountService.getAccounts();
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());
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
            account = accountService.delete(accountId);
            response.setStatus(Response.Status.ACCEPTED.getStatusCode());
            return new PaymentResponse<Account>(header, account);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return new PaymentResponse<Account>(header.success(false).status(Response.Status.BAD_REQUEST.getStatusCode()).errorCode(e.getMessage()), account);
        }
    }


}
