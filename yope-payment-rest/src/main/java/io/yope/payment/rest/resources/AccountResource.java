package io.yope.payment.rest.resources;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Account.Type;
import io.yope.payment.exceptions.DuplicateEmailException;
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
        try {
            if (Type.ADMIN.equals(registrationRequest.getType())) {
                return badRequest("type", "Wrong User Type; please use either SELLER or BUYER");
            }
            final Account to = accountHelper.registerAccount(registrationRequest);
            final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
            response.setStatus(Response.Status.CREATED.getStatusCode());
            return new PaymentResponse<Account>(header, to);
        } catch (final DuplicateEmailException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return badRequest("email", e.getMessage());
        } catch (final Exception e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return serverError(e.getMessage());
        }
    }

    /**
     * Update Personal Account.
     * @param account
     * @return
     */
    @RequestMapping(value="/me", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Account> updateAccount(final HttpServletResponse response,
                                                                @RequestBody(required=false) final Account account) {
        return doUpdateAccount(response, getLoggedAccount().getId(), account);
    }

    /**
     * Get Personal Account.
     * @return
     */
    @RequestMapping(value="/me", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PaymentResponse<Account> getAccount() {
        final Account loggedAccount = getLoggedAccount();
        if (loggedAccount == null) {
            return unauthorized();
        }
        final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
        return new PaymentResponse<Account>(header, loggedAccount);
    }

    /**
     * Delete Personal Account.
     * @return
     */
    @RequestMapping(value="/me", method = RequestMethod.DELETE, produces = "application/json")
    public @ResponseBody PaymentResponse<Account> deleteAccount(final HttpServletResponse response) {
        final Account loggedAccount = getLoggedAccount();
        if (loggedAccount == null) {
            return unauthorized();
        }
        return doDeleteAccount(response, loggedAccount.getId());
    }


}
