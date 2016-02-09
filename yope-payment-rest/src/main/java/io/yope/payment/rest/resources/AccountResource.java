package io.yope.payment.rest.resources;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.core.Response;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Collections2;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Account.Type;
import io.yope.payment.exceptions.DuplicateEmailException;
import io.yope.payment.requests.RegistrationRequest;

/**
 * Account resource.
 */
//@Controller
//@EnableAutoConfiguration
@RestController
@RequestMapping("/accounts")
public class AccountResource extends BaseResource {

    /**
     * Create Account.
     * @param response
     * @param registrationRequest
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public PaymentResponse<Account> createAccount(
            final HttpServletResponse response,
            @RequestBody(required=true) @Valid final RegistrationRequest registrationRequest,
            final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            final List<Error> errors = getErrors(bindingResult);
            return badRequest(errors);
        }
        try {
            if (Type.ADMIN.equals(registrationRequest.getType())) {
                return badRequest("type", "Wrong User Type; please use either SELLER or BUYER");
            }
            final Account account = accountService.registerAccount(registrationRequest);
            securityService.createUser(registrationRequest.getEmail(), registrationRequest.getPassword(), registrationRequest.getType().toString());
            final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
            response.setStatus(Response.Status.CREATED.getStatusCode());
            return new PaymentResponse<Account>(header, account);
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
    @PreAuthorize("hasAuthority('ROLE_DOMAIN_USER')")
    @RequestMapping(value="/me", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Account> updateAccount(final HttpServletResponse response,
                                                                @RequestBody(required=false) final Account account) {
        return doUpdateAccount(response, getLoggedAccount().getId(), account);
    }

    /**
     * Get Personal Account.
     * @return
     */
    @PreAuthorize("hasAuthority('ROLE_DOMAIN_USER')")
    @RequestMapping(value="/me", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PaymentResponse<Account> getAccount() {
        final Account loggedAccount = getLoggedAccount();
        final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
        return new PaymentResponse<Account>(header, loggedAccount);
    }

    /**
     * Delete Personal Account.
     * @return
     */
    @PreAuthorize("hasAuthority('ROLE_DOMAIN_USER')")
    @RequestMapping(value="/me", method = RequestMethod.DELETE, produces = "application/json")
    public @ResponseBody PaymentResponse<Account> deleteAccount(final HttpServletResponse response) {
        final Account loggedAccount = getLoggedAccount();
        return doDeleteAccount(response, loggedAccount.getId());
    }

    private List<Error> getErrors(final BindingResult bindingResult) {
        final List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        return Lists.newArrayList(Collections2.transform(
                fieldErrors, error -> Error.builder().field(error.getField()).message(error.getDefaultMessage()).build()
        ));
    }


}
