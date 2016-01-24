package io.yope.payment.rest.resources;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Direction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.AuthorizationException;
import io.yope.payment.exceptions.BadRequestException;
import io.yope.payment.exceptions.ObjectNotFoundException;

/**
 * Wallet Resource.
 */
@RequestMapping("/wallets")
@PreAuthorize("hasAuthority('ROLE_DOMAIN_USER')")
@RestController
public class WalletResource extends BaseResource {

    /**
     * Creates a new wallet. New wallet is pending until confirmation.
     * @param wallet
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public PaymentResponse<Wallet> createWallet(
            final HttpServletResponse response,
            @RequestBody(required=true) final Wallet wallet) {
        final ResponseHeader header = new ResponseHeader(true, Response.Status.CREATED.getStatusCode());
        try {
            final Account loggedAccount = getLoggedAccount();
            final Wallet saved = accountService.createWallet(loggedAccount, wallet);
            response.setStatus(Response.Status.CREATED.getStatusCode());
            return new PaymentResponse<Wallet>(header, saved);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(e.getMessage());
        } catch (final BadRequestException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return this.badRequest(e.field(), e.getMessage());
        } catch (final Exception e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return this.serverError(e.getMessage());
        }
    }

    /**
     * Modifies a wallet.
     * @param wallet
     * @param response
     * @return
     */
    @RequestMapping(value="/{walletId}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public PaymentResponse<Wallet> updateWallet(
            @PathVariable("walletId") final Long walletId,
            @RequestBody(required=false) final Wallet wallet,
            final HttpServletResponse response) {
        try {
            this.checkOwnership(walletId);
            return doUpdateWallet(walletId, wallet, response);
        } catch (final AuthorizationException e) {
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            return this.unauthorized();
        }
    }


    /**
     * Get Wallets by Account ID.
     * @param accountId
     * @param response
     * @param status the status of the wallets being returned
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public PaymentResponse<List<Wallet>> getWallets(final HttpServletResponse response,
           @RequestParam(value="status", required=false, defaultValue="ACTIVE") final String status) {
       final Account loggedAccount = getLoggedAccount();
        return this.getWallets(response, loggedAccount.getId(), status);
    }

    /**
     * Get Wallet By ID.
     * @param accountId
     * @param walletId
     * @param response
     * @return
     */
    @RequestMapping(value="/{walletId}", method = RequestMethod.GET, produces = "application/json")
    public PaymentResponse<Wallet> getWallet(@PathVariable("walletId") final Long walletId,
                                                           final HttpServletResponse response) {
        try {
            this.checkOwnership(walletId);
            return retrieveWallet(walletId, response);
        } catch (final AuthorizationException e) {
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            return this.unauthorized();
        }
    }

    /**
     * Get Transactions for Wallet Id.
     * @param walletId
     * @param reference
     * @param response
     * @return
     */
    @RequestMapping(value="/{walletId}/transactions", method = RequestMethod.GET, produces = "application/json")
    public PaymentResponse<List<Transaction>> getTransactions(@PathVariable("walletId") final Long walletId,
           @RequestParam(value="reference", required=false) final String reference,
           @RequestParam(value="dir", required=false, defaultValue = "BOTH") final Direction direction,
           @RequestParam(value="status", required=false) final Status status,
           @RequestParam(value="type", required=false) final Transaction.Type type,
           final HttpServletResponse response) {
        final Account loggedAccount = getLoggedAccount();
        if (!accountService.owns(loggedAccount, walletId)) {
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            return this.unauthorized();
        }
        return getWalletTransactions(walletId, reference, direction, status, type, response);
    }

    /**
     * Delete Wallet.
     * @param walletId
     * @param response
     * @return
     */
    @RequestMapping(value="/{walletId}", method = RequestMethod.DELETE, produces = "application/json")
    public PaymentResponse<Wallet> deactivateWallet(@PathVariable("walletId") final Long walletId,
                                                                  final HttpServletResponse response) {
        new ResponseHeader(true, Response.Status.ACCEPTED.getStatusCode());
        try {
            this.checkOwnership(walletId);
            return doDeleteWallet(walletId, response);
        } catch (final AuthorizationException e) {
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            return this.unauthorized();
        }
    }

}
