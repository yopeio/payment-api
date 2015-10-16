package io.yope.payment.rest.resources;

import java.text.MessageFormat;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
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
import io.yope.payment.domain.Transaction.Direction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.TransactionTO;
import io.yope.payment.domain.transferobjects.WalletTO;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.rest.BadRequestException;
import io.yope.payment.rest.helpers.TransactionHelper;
import io.yope.payment.rest.helpers.WalletHelper;

/**
 * Wallet Resource.
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/wallets")
public class WalletResource extends BaseResource {

    @Autowired
    private TransactionHelper transactionHelper;

    @Autowired
    private WalletHelper walletHelper;

    private static final String WALLET_NOT_FOUND = "Wallet with id {0} not found";

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
            final Account loggedAccount = getLoggedAccount();
            final Wallet saved = accountHelper.createWallet(loggedAccount, wallet);
            response.setStatus(Response.Status.CREATED.getStatusCode());
            return new PaymentResponse<Wallet>(header, saved);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(wallet, e.getMessage());
        } catch (final BadRequestException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return this.badRequest(wallet, e.getMessage());
        }
    }

    /**
     * Modifies a wallet.
     * @param wallet
     * @param response
     * @return
     */
    @RequestMapping(value="/{walletId}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Wallet> updateWallet(
            @PathVariable final long walletId,
            @RequestBody(required=false) final WalletTO wallet,
            final HttpServletResponse response) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.ACCEPTED.getStatusCode());
        if (!walletHelper.exists(walletId)) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(null, MessageFormat.format(WALLET_NOT_FOUND, walletId));
        }
        final Account loggedAccount = getLoggedAccount();
        if (!accountHelper.owns(loggedAccount, walletId)) {
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            return this.unauthorized(wallet);
        }
        try {
            final Wallet saved = walletHelper.update(walletId, wallet);
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
        final Account loggedAccount = getLoggedAccount();
        final List<Wallet> wallets = walletHelper.get(loggedAccount.getId());
        return new PaymentResponse<List<Wallet>>(header, wallets);
    }

    /**
     * Get Wallet By ID.
     * @param accountId
     * @param walletId
     * @param response
     * @return
     */
    @RequestMapping(value="/{walletId}", method = RequestMethod.GET, produces = "application/json")
    public PaymentResponse<Wallet> getWallet(@PathVariable final Long walletId,
                                             final HttpServletResponse response) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());

        if (!walletHelper.exists(walletId)) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(null, MessageFormat.format(WALLET_NOT_FOUND, walletId));
        }
        final Account loggedAccount = getLoggedAccount();
        if (!accountHelper.owns(loggedAccount, walletId)) {
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            return this.unauthorized(null);
        }

        final Wallet wallet = walletHelper.getById(walletId);
        return new PaymentResponse<Wallet>(header, wallet);
    }

    /**
     * Get Transactions for Wallet Id.
     * @param walletId
     * @param reference
     * @param response
     * @return
     */
    @RequestMapping(value="/{walletId}/transactions", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<List<TransactionTO>> getTransactions(@PathVariable final Long walletId,
           @RequestParam(value="reference", required=false) final String reference,
           @RequestParam(value="dir", required=false, defaultValue = "BOTH") final Direction direction,
           @RequestParam(value="status", required=false) final Status status,
           @RequestParam(value="type", required=false) final Transaction.Type type,
           final HttpServletResponse response) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());

        final Account loggedAccount = getLoggedAccount();
        if (!walletHelper.exists(walletId)) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(null, MessageFormat.format(WALLET_NOT_FOUND, walletId));
        }
        if (!accountHelper.owns(loggedAccount, walletId)) {
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            return this.unauthorized(null);
        }
        try {
            final List<TransactionTO> transactions = transactionHelper.getTransactionsForWallet(walletId, reference, direction, status, type);
            return new PaymentResponse<List<TransactionTO>>(header, transactions);
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
        final Account loggedAccount = getLoggedAccount();
        if (!walletHelper.exists(walletId)) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(null, MessageFormat.format(WALLET_NOT_FOUND, walletId));
        }
        if (accountHelper.owns(loggedAccount, walletId)
            || loggedAccount.getType().equals(Type.ADMIN)) {
            try {
                response.setStatus(Response.Status.ACCEPTED.getStatusCode());
                return new PaymentResponse<Wallet>(header, walletHelper.delete(walletId));
            } catch (final ObjectNotFoundException e) {
                response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
                return this.notFound(null, e.getMessage());
            }
        }
        response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        return this.unauthorized(null);
    }

}
