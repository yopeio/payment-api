/**
 *
 */
package io.yope.payment.rest.resources;

import java.text.MessageFormat;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import io.yope.services.UserSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Direction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.domain.Transaction.Type;
import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.AuthorizationException;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.TransactionService;
import io.yope.payment.services.WalletService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mgerardi
 *
 */
@Slf4j
public abstract class BaseResource {

    public static final String WALLET_NOT_FOUND = "Wallet with id {0} not found";

    public static final String ACCOUND_NOT_FOUND = "Account with id {0} not found";

    @Autowired
    protected AccountService accountService;

    @Autowired
    protected TransactionService transactionService;

    @Autowired
    protected WalletService walletService;

    @Autowired
    protected UserSecurityService securityService;

    protected Account getLoggedAccount() {
        final User user = this.securityService.getCurrentUser();
        if (user == null) {
            return null;
        }
        final Account account = this.accountService.getByEmail(user.getUsername());
        log.info("logged as {}", account);
        return account;
    }

    protected void checkPermission(final Account.Type type) throws AuthorizationException {
        final Account loggedAccount = this.getLoggedAccount();
        if (!type.equals(loggedAccount.getType())) {
            throw new AuthorizationException();
        }
    }

    protected void checkOwnership(final Long walletId) throws AuthorizationException {
        final Account loggedAccount = this.getLoggedAccount();
        if (!this.accountService.owns(loggedAccount, walletId)) {
            throw new AuthorizationException();
        }
    }

    protected void checkOwnership(final Transaction transaction) throws AuthorizationException {
        final Account loggedAccount = this.getLoggedAccount();
        if (!this.accountService.owns(loggedAccount, transaction.getSource().getId())
         && !this.accountService.owns(loggedAccount, transaction.getDestination().getId())) {
            throw new AuthorizationException();
        }
    }

    private <T> PaymentResponse<T> error(final String field, final String message, final Response.Status status) {
        final ResponseHeader header = new ResponseHeader(false, status.getStatusCode());
        final Error error = Error.builder().field(field).message(message).build();
        return new PaymentResponse<T>(header, error);
    }

    protected <T> PaymentResponse<T> unauthorized() {
        return this.error(null, Response.Status.UNAUTHORIZED.toString(), Response.Status.UNAUTHORIZED);
    }

    protected <T> PaymentResponse<T> serverError(final String message) {
        return this.error(null, message, Response.Status.INTERNAL_SERVER_ERROR);
    }

    protected <T> PaymentResponse<T> badRequest(final String field, final String message) {
        return this.error(field, message, Response.Status.BAD_REQUEST);
    }

    protected <T> PaymentResponse<T> badRequest(final String message) {
        return this.error(null, message, Response.Status.BAD_REQUEST);
    }

    protected <T> PaymentResponse<T> badRequest(final List<Error> errors) {
        final ResponseHeader header = new ResponseHeader(false, Response.Status.BAD_REQUEST.getStatusCode());
        return new PaymentResponse<T>(header, errors);
    }

    protected <T> PaymentResponse<T> notFound(final String message) {
        return this.error(null, message, Response.Status.NOT_FOUND);
    }


    protected PaymentResponse<Account> doUpdateAccount(final HttpServletResponse response, final Long accountId, final Account account) {
        final ResponseHeader header = new ResponseHeader(true, Response.Status.ACCEPTED.getStatusCode());
        try {
            final Account updated = this.accountService.update(accountId, account);
            return new PaymentResponse<Account>(header, updated);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(e.getMessage());
        } catch (final Exception e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return this.serverError(e.getMessage());
        }
    }

    protected PaymentResponse<Account> doDeleteAccount(final HttpServletResponse response, final Long accountId) {
        final ResponseHeader header = new ResponseHeader(true, Response.Status.ACCEPTED.getStatusCode());
        try {
            final Account account = this.accountService.delete(accountId);
            response.setStatus(Response.Status.ACCEPTED.getStatusCode());
            return new PaymentResponse<Account>(header, account);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(e.getMessage());
        } catch (final Exception e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return this.serverError(e.getMessage());
        }
    }

    protected PaymentResponse<List<Wallet>> getWallets(final HttpServletResponse response, final Long accountId, final String status) {
        final Wallet.Status statusFilter = this.getStatus(status);
        final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
        final List<Wallet> wallets = this.walletService.get(accountId, statusFilter);
        return new PaymentResponse<List<Wallet>>(header, wallets);
    }

    private io.yope.payment.domain.Wallet.Status getStatus(final String status) {
        try {
            if (status.equalsIgnoreCase("all")) {
                return null;
            }
            return Wallet.Status.valueOf(status);
        } catch (final Exception e) {
            return Wallet.Status.ACTIVE;
        }
    }

    protected PaymentResponse<Wallet> retrieveWallet(final Long walletId, final HttpServletResponse response) {
        final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
        if (!this.walletService.exists(walletId)) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(MessageFormat.format(WALLET_NOT_FOUND, walletId));
        }
        final Wallet wallet = this.walletService.getById(walletId);
        return new PaymentResponse<Wallet>(header, wallet);
    }

    protected PaymentResponse<Wallet> doDeleteWallet(final long walletId, final HttpServletResponse response) {
        final ResponseHeader header = new ResponseHeader(true, Response.Status.ACCEPTED.getStatusCode());
        if (!this.walletService.exists(walletId)) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(MessageFormat.format(WALLET_NOT_FOUND, walletId));
        }
        try {
            response.setStatus(Response.Status.ACCEPTED.getStatusCode());
            return new PaymentResponse<Wallet>(header, this.walletService.delete(walletId));
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(e.getMessage());
        } catch (final Exception e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return this.serverError(e.getMessage());
        }
    }

    protected PaymentResponse<Wallet> doUpdateWallet(final long walletId, final Wallet wallet, final HttpServletResponse response) {
        final ResponseHeader header = new ResponseHeader(true, Response.Status.ACCEPTED.getStatusCode());
        try {
            final Wallet saved = this.walletService.update(walletId, wallet);
            return new PaymentResponse<Wallet>(header, saved);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return this.badRequest(e.getMessage());
        } catch (final Exception e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return this.serverError(e.getMessage());
        }
    }

    protected PaymentResponse<List<Transaction>> getAccountTransactions(final HttpServletResponse response,
                                                                        final Long accountId,
                                                                        final String reference,
                                                                        final Direction direction,
                                                                        final Status status,
                                                                        final Transaction.Type type) {
        final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
        if (this.accountService.exists(accountId)) {
            return this.badRequest(MessageFormat.format(WALLET_NOT_FOUND, accountId));
        }
        List<Transaction> transactions = null;
        try {
            transactions = this.transactionService.getTransactionsForAccount(accountId, reference, direction, status, type);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(e.getMessage());
        } catch (final Exception e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return this.serverError(e.getMessage());
        }
        return new PaymentResponse<List<Transaction>>(header, transactions);
    }


    protected PaymentResponse<List<Transaction>> getWalletTransactions(final Long walletId, final String reference,
                                                                       final Direction direction, final Status status, final Type type, final HttpServletResponse response) {
        final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
        if (!this.walletService.exists(walletId)) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(MessageFormat.format(WALLET_NOT_FOUND, walletId));
        }
        try {
            final List<Transaction> transactions = this.transactionService.getTransactionsForWallet(walletId, reference, direction, status, type);
            return new PaymentResponse<List<Transaction>>(header, transactions);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(e.getMessage());
        } catch (final Exception e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return this.serverError(e.getMessage());
        }
    }
}
