package io.yope.payment.rest.resources;

import java.text.MessageFormat;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.security.access.prepost.PreAuthorize;
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
import io.yope.payment.exceptions.AuthorizationException;
import io.yope.payment.exceptions.IllegalTransactionStateException;
import io.yope.payment.exceptions.InsufficientFundsException;
import io.yope.payment.exceptions.ObjectNotFoundException;

//@Controller
//@EnableAutoConfiguration
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_BACKEND_ADMIN')")
public class AdminResource extends BaseResource {

    /**
     * List of existing accounts.
     * @param accountId
     * @return
     * @throws AuthorizationException
     */
    @RequestMapping(value="/accounts", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<List<Account>> getAccounts() throws AuthorizationException {
        this.checkPermission(Type.ADMIN);
        final List<Account> accounts = this.accountService.getAccounts();
        final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
        return new PaymentResponse<List<Account>>(header, accounts);
    }

    /**
     * set a new status  for a transaction.
     * it is open only to administrator.
     * @param response
     * @param transactionId
     * @param status
     * @return
     * @throws AuthorizationException
     */
    @RequestMapping(value="/transactions/{transactionId}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Transaction> modify(
            final HttpServletResponse response,
            @PathVariable final Long transactionId,
            @RequestParam(value="status", required=true) final Status status) {
        final ResponseHeader header = new ResponseHeader(true, Response.Status.ACCEPTED.getStatusCode());
        try {
            this.checkPermission(Type.ADMIN);
            final Transaction saved = this.transactionService.save(transactionId, status);
            response.setStatus(Response.Status.ACCEPTED.getStatusCode());
            return new PaymentResponse<Transaction>(header, saved);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(e.getMessage());
        } catch (final InsufficientFundsException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return this.badRequest(null ,e.getMessage());
        } catch (final IllegalTransactionStateException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return this.badRequest(null, e.getMessage());
        } catch (final Exception e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return this.serverError(e.getMessage());
        }
    }

    /**
     * Update Account.
     * @param accountId
     * @param account
     * @return
     * @throws AuthorizationException
     */
    @RequestMapping(value="/accounts/{accountId}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Account> updateAccount(final HttpServletResponse response,
                                                                @PathVariable("accountId") final Long accountId,
                                                                @RequestBody(required=false) final Account account) throws AuthorizationException {
        this.checkPermission(Type.ADMIN);
        return this.doUpdateAccount(response, accountId, account);
    }

    /**
     * Get Account By ID.
     * @param accountId
     * @return
     * @throws AuthorizationException
     */
    @RequestMapping(value="/accounts/{accountId}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PaymentResponse<Account> getAccount(@PathVariable("accountId") final Long accountId,
                                                             final HttpServletResponse response) throws AuthorizationException {
        this.checkPermission(Type.ADMIN);
        try {
            final Account account = this.accountService.getById(accountId);
            if (account == null) {
                return this.notFound(MessageFormat.format("Account {0} not found", accountId));
            }
            final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
            return new PaymentResponse<Account>(header, account);
        } catch (final Exception e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return this.serverError(e.getMessage());
        }
    }

    /**
     * Delete Account.
     * @return
     * @throws AuthorizationException
     */
    @RequestMapping(value="/accounts/{accountId}", method = RequestMethod.DELETE, produces = "application/json")
    public @ResponseBody PaymentResponse<Account> deleteAccount(final HttpServletResponse response,
                                                                @PathVariable("accountId") final Long accountId) throws AuthorizationException {
        this.checkPermission(Type.ADMIN);
        return this.doDeleteAccount(response, accountId);
    }

    /**
     * Get Wallets by Account ID.
     * @param accountId
     * @param response
     * @return
     */
    @RequestMapping(value="/accounts/{accountId}/wallets", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<List<Wallet>> getWallets(
            @PathVariable("accountId") final Long accountId,
            @RequestParam(value="status", required=false, defaultValue="ACTIVE") final String status,
            final HttpServletResponse response)  throws AuthorizationException {
        this.checkPermission(Type.ADMIN);
        return this.getWallets(response, accountId, status);
    }

    /**
     * Get Wallet By ID.
     * @param accountId
     * @param walletId
     * @param response
     * @return
     * @throws AuthorizationException
     */
    @RequestMapping(value="/wallets/{walletId}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PaymentResponse<Wallet> getWallet(@PathVariable final Long walletId,
                                                           final HttpServletResponse response) throws AuthorizationException {
        this.checkPermission(Type.ADMIN);
        return this.retrieveWallet(walletId, response);
    }

    /**
     * Modifies a wallet.
     * @param wallet
     * @param response
     * @return
     */
    @RequestMapping(value="/wallets/{walletId}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Wallet> updateWallet(
            @PathVariable final long walletId,
            @RequestBody(required=false) final Wallet wallet,
            final HttpServletResponse response) throws AuthorizationException {
        this.checkPermission(Type.ADMIN);
        return this.doUpdateWallet(walletId, wallet, response);
    }

    /**
     * Delete Wallet.
     * @param accountId
     * @param walletId
     * @param response
     * @return
     */
    @RequestMapping(value="/wallets/{walletId}", method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
    public PaymentResponse<Wallet> deactivateWallet(@PathVariable final long walletId,
                                                    final HttpServletResponse response) throws AuthorizationException {
        this.checkPermission(Type.ADMIN);
        return this.doDeleteWallet(walletId, response);
    }

    /**
     * retrieves an account's transactions.
     * @param accountId
     * @return
     */
    @RequestMapping(value="/accounts/{accountId}/transactions", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PaymentResponse<List<Transaction>> getTransactions(final HttpServletResponse response,
                                                                            @PathVariable final Long accountId,
                                                                            @RequestParam(value="reference", required=false) final String reference,
                                                                            @RequestParam(value="dir", required=false, defaultValue = "BOTH") final Direction direction,
                                                                            @RequestParam(value="status", required=false) final Status status,
                                                                            @RequestParam(value="type", required=false) final Transaction.Type type) throws AuthorizationException {
        this.checkPermission(Type.ADMIN);
        return this.getAccountTransactions(response, accountId, reference, direction, status, type);
    }

    @RequestMapping(value="/transactions/{transactionId}", method = RequestMethod.GET, consumes = "application/json", produces = "application/json", params= {"senderHash"})
    public @ResponseBody PaymentResponse<Transaction> getBySenderHash(
            @PathVariable("transactionId") final Long transactionId) throws AuthorizationException {
        this.checkPermission(Type.ADMIN);
        final Transaction transaction = this.transactionService.getTransactionById(transactionId);
        if (transaction == null) {
            return this.notFound("Not found " + transactionId);
        }
        final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
        return new PaymentResponse<Transaction>(header, transaction);
    }

    @RequestMapping(value="/transactions", method = RequestMethod.GET, consumes = "application/json", produces = "application/json", params= {"senderHash"})
    public @ResponseBody PaymentResponse<Transaction> getBySenderHash(@RequestParam(value="senderHash", required=true)  final String hash) throws AuthorizationException {
        this.checkPermission(Type.ADMIN);
        final Transaction transaction = this.transactionService.getTransactionBySenderHash(hash);
        if (transaction == null) {
            return this.notFound(hash);
        }
        final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
        return new PaymentResponse<Transaction>(header, transaction);
    }

    @RequestMapping(value="/transactions", method = RequestMethod.GET, consumes = "application/json", produces = "application/json", params= {"receiverHash"})
    public @ResponseBody PaymentResponse<Transaction> getByReceiverHash(@RequestParam(value="receiverHash", required=true)  final String hash) throws AuthorizationException {
        this.checkPermission(Type.ADMIN);
        final Transaction transaction = this.transactionService.getTransactionByReceiverHash(hash);
        if (transaction == null) {
            return this.notFound(hash);
        }
        final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
        return new PaymentResponse<Transaction>(header, transaction);
    }

    @RequestMapping(value="/transactions", method = RequestMethod.GET, consumes = "application/json", produces = "application/json", params= {"hash"})
    public @ResponseBody PaymentResponse<Transaction> getByTransactionHash(@RequestParam(value="hash", required=true)  final String hash) throws AuthorizationException {
        this.checkPermission(Type.ADMIN);
        final Transaction transaction = this.transactionService.getTransactionByHash(hash);
        if (transaction == null) {
            return this.notFound(hash);
        }
        final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
        return new PaymentResponse<Transaction>(header, transaction);
    }


    /**
     * Get Transactions for Wallet Id.
     * @param walletId
     * @param reference
     * @param response
     * @return
     */
    @RequestMapping(value="/wallets/{walletId}/transactions", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<List<Transaction>> getTransactions(@PathVariable final Long walletId,
                                                                            @RequestParam(value="reference", required=false) final String reference,
                                                                            @RequestParam(value="dir", required=false, defaultValue = "BOTH") final Direction direction,
                                                                            @RequestParam(value="status", required=false) final Status status,
                                                                            @RequestParam(value="type", required=false) final Transaction.Type type,
                                                                            final HttpServletResponse response)  throws AuthorizationException {
        this.checkPermission(Type.ADMIN);
        return this.getWalletTransactions(walletId, reference, direction, status, type, response);
    }
}
