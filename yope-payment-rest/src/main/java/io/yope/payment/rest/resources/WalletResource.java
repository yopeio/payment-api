package io.yope.payment.rest.resources;

import java.text.MessageFormat;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Account.Type;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.WalletTO;
import io.yope.payment.exceptions.ObjectNotFoundException;

/**
 * Wallet Resource.
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/wallets")
public class WalletResource extends BaseResource {

    /**
     * Creates a new wallet. New wallet is pending until confirmation.
     * @param wallet
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Wallet> createWallet(
            final HttpServletResponse response,
            @RequestBody(required=false) final WalletTO wallet) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.CREATED.getStatusCode());
        try {
            final Account loggedAccount = this.getLoggedAccount();
            final Wallet saved = this.accountHelper.createWallet(loggedAccount, wallet);
            response.setStatus(Response.Status.CREATED.getStatusCode());
            return new PaymentResponse<Wallet>(header, saved);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(null, e.getMessage());
        }
    }

    /**
     * Modifies a wallet.
     * @param wallet
     * @param response
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Wallet> updateWallet(
            @RequestBody(required=false) final WalletTO wallet,
            final HttpServletResponse response) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.ACCEPTED.getStatusCode());

        final Account loggedAccount = this.getLoggedAccount();
        if (this.accountHelper.owns(loggedAccount, wallet.getId())) {
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            return this.unauthorized(wallet);
        }
        try {
            final Wallet saved = this.walletService.update(wallet.getId(), wallet);
            return new PaymentResponse<Wallet>(header, saved);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(null, e.getMessage());
        }
    }

    /**
     * Get Wallets by Account ID.
     * @param accountId
     * @param response
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<List<Wallet>> getWallets(final HttpServletResponse response) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());
        final Account loggedAccount = this.getLoggedAccount();
        final List<Wallet> wallets = this.walletService.get(loggedAccount.getId());
        return new PaymentResponse<List<Wallet>>(header, wallets);
    }

    /**
     * Get Wallet By ID.
     * @param accountId
     * @param walletId
     * @param response
     * @return
     */
    @RequestMapping(value="/{walletId}", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public PaymentResponse<Wallet> getWallet(@PathVariable final long walletId,
                                             final HttpServletResponse response) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());

        final Account loggedAccount = this.getLoggedAccount();
        if (this.accountHelper.owns(loggedAccount, walletId)) {
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            return new PaymentResponse<Wallet>(header.success(false).status(Response.Status.UNAUTHORIZED.getStatusCode()).errorCode(Response.Status.UNAUTHORIZED.toString()), null);
        }

        final Wallet wallet = this.walletService.getById(walletId);
        if (wallet != null) {
            return new PaymentResponse<Wallet>(header, wallet);
        } else {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return this.badRequest(null, MessageFormat.format("Wallet with id {0} not found", walletId));
        }
    }

    /**
     * Get Transactions for Wallet Id.
     * @param walletId
     * @param reference
     * @param response
     * @return
     */
    @RequestMapping(value="/{walletId}/transactions", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<List<Transaction>> getForWallet(@PathVariable final Long walletId,
           @RequestParam(value="reference", required=false) final String reference,
           final HttpServletResponse response) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());

        final Account loggedAccount = this.getLoggedAccount();
        if (this.accountHelper.owns(loggedAccount, walletId)) {
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            return new PaymentResponse<List<Transaction>>(header.success(false).status(Response.Status.UNAUTHORIZED.getStatusCode()).errorCode(Response.Status.UNAUTHORIZED.toString()), null);
        }
        final Wallet wallet = this.walletService.getById(walletId);
        if (wallet == null) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return new PaymentResponse<List<Transaction>>(header.success(false).status(Response.Status.NOT_FOUND.getStatusCode()).errorCode(MessageFormat.format("Wallet with id {0} not found", walletId)), null);
        }
        final String hash = wallet.getHash();
        List<Transaction> transactions = null;
        try {
            transactions = this.transactionService.getForWallet(hash, reference, Transaction.Direction.BOTH);
            return new PaymentResponse<List<Transaction>>(header, transactions);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(null, e.getMessage());
        }
    }

    /**
     * Delete Wallet.
     * @param accountId
     * @param walletId
     * @param response
     * @return
     */
    @RequestMapping(value="/{walletId}", method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
    public PaymentResponse<Wallet> deactivateWallet(@PathVariable final long walletId,
                                                    final HttpServletResponse response) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.ACCEPTED.getStatusCode());
        final Account loggedAccount = this.getLoggedAccount();
        if (this.accountHelper.owns(loggedAccount, walletId)
            || loggedAccount.getType().equals(Type.ADMIN)) {
            try {
                response.setStatus(Response.Status.ACCEPTED.getStatusCode());
                return new PaymentResponse<Wallet>(header, this.walletService.delete(walletId));
            } catch (final ObjectNotFoundException e) {
                response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
                return this.notFound(null, e.getMessage());
            }
        }
        response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        return this.unauthorized(null);
    }

}
