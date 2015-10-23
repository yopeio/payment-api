package io.yope.payment.rest.resources;

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

import io.yope.payment.blockchain.BlockchainException;
import io.yope.payment.domain.Account;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Direction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.domain.Transaction.Type;
import io.yope.payment.exceptions.AuthorizationException;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.rest.BadRequestException;
import io.yope.payment.rest.helpers.AccountHelper;
import io.yope.payment.rest.helpers.TransactionHelper;

/**
 * Wallet Resource.
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/transactions")
public class TransactionResource extends BaseResource {

    @Autowired
    private AccountHelper accountHelper;

    @Autowired
    private TransactionHelper transactionHelper;

    /**
     * Create Transaction.
     *
     * @param transaction
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Transaction> create(final HttpServletResponse response,
            @RequestBody(required = true) final Transaction transaction) {
        final ResponseHeader header = new ResponseHeader(true, Response.Status.CREATED.getStatusCode());
        final Account loggedAccount = getLoggedAccount();
        try {
            final Transaction saved = transactionHelper.create(transaction, loggedAccount.getId());
            response.setStatus(Response.Status.CREATED.getStatusCode());
            return new PaymentResponse<Transaction>(header, saved);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return serverError(e.getMessage());
        } catch (final BlockchainException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return serverError(e.getMessage());
        } catch (final BadRequestException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return badRequest(e.getField(), e.getMessage());
        } catch (final Exception e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return serverError(e.getMessage());
        }
    }

    @RequestMapping(value = "/{transactionId}", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Transaction> get(@PathVariable final long transactionId) {
        final Account loggedAccount = getLoggedAccount();
        final Transaction transaction = transactionHelper.getTransactionById(transactionId);
        if (transaction == null) {
            return notFound("Not found " + transactionId);
        }
        final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
        if (accountHelper.owns(loggedAccount, transaction.getSource().getId())
                || accountHelper.owns(loggedAccount, transaction.getDestination().getId())) {
            return new PaymentResponse<Transaction>(header, transaction);
        }
        return unauthorized();
    }

    @RequestMapping(method = RequestMethod.GET, consumes = "application/json", produces = "application/json", params = {"senderHash" })
    public @ResponseBody PaymentResponse<Transaction> getBySenderHash(
            @RequestParam(value = "senderHash", required = true) final String hash) throws AuthorizationException {
        final Transaction transaction = transactionHelper.getTransactionBySenderHash(hash);
        if (transaction == null) {
            return notFound(hash);
        }
        final Account loggedAccount = getLoggedAccount();
        if (accountHelper.owns(loggedAccount, transaction.getSource().getId())
                || accountHelper.owns(loggedAccount, transaction.getDestination().getId())) {
            final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
            return new PaymentResponse<Transaction>(header, transaction);
        }
        return unauthorized();
    }

    @RequestMapping(method = RequestMethod.GET, consumes = "application/json", produces = "application/json", params = {"receiverHash" })
    public @ResponseBody PaymentResponse<Transaction> getByReceiverHash(
            @RequestParam(value = "receiverHash", required = true) final String hash) throws AuthorizationException {
        final Transaction transaction = transactionHelper.getTransactionByReceiverHash(hash);
        if (transaction == null) {
            return notFound(hash);
        }
        final Account loggedAccount = getLoggedAccount();
        if (accountHelper.owns(loggedAccount, transaction.getSource().getId())
                || accountHelper.owns(loggedAccount, transaction.getDestination().getId())) {
            final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
            return new PaymentResponse<Transaction>(header, transaction);
        }
        return unauthorized();
    }

    @RequestMapping(method = RequestMethod.GET, consumes = "application/json", produces = "application/json", params = {"hash" })
    public @ResponseBody PaymentResponse<Transaction> getByTransactionHash(
            @RequestParam(value = "hash", required = true) final String hash) throws AuthorizationException {
        final Transaction transaction = transactionHelper.getTransactionByHash(hash);
        if (transaction == null) {
            return notFound(hash);
        }
        final Account loggedAccount = getLoggedAccount();
        if (accountHelper.owns(loggedAccount, transaction.getSource().getId())
                || accountHelper.owns(loggedAccount, transaction.getDestination().getId())) {
            final ResponseHeader header = new ResponseHeader(true, Response.Status.OK.getStatusCode());
            return new PaymentResponse<Transaction>(header, transaction);
        }
        return unauthorized();
    }

    /**
     * retrieves an account's transactions.
     *
     * @param accountId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PaymentResponse<List<Transaction>> getTransactions(final HttpServletResponse response,
            @RequestParam(value = "reference", required = false) final String reference,
            @RequestParam(value = "dir", required = false, defaultValue = "BOTH") final Direction direction,
            @RequestParam(value = "status", required = false) final Status status,
            @RequestParam(value = "type", required = false) final Type type) {

        final Account loggedAccount = getLoggedAccount();
        final Long accountId = loggedAccount.getId();
        return getAccountTransactions(response, accountId, reference, direction, status, type);
    }

}
