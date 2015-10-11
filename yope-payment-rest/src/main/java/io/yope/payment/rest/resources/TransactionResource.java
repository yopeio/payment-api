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

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.domain.transferobjects.TransactionTO;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.rest.helpers.AccountHelper;

/**
 * Wallet Resource.
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/transactions")
public class TransactionResource extends BaseResource {

    @Autowired
    private AccountHelper accountHelper;

    /**
     * Create Transaction.
     * @param transaction
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Transaction> create(
            final HttpServletResponse response,
            @RequestBody(required=true) final TransactionTO transaction) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.CREATED.getStatusCode());
        try {
            final Transaction saved = transactionService.create(transaction);
            response.setStatus(Response.Status.CREATED.getStatusCode());
            return new PaymentResponse<Transaction>(header, saved);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return this.badRequest(transaction, e.getMessage());
        }
    }

    @RequestMapping(value="/{transactionId}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Transaction> modify(
            final HttpServletResponse response,
            @PathVariable final Long transactionId,
            @RequestParam(value="status", required=true) final Status status) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.ACCEPTED.getStatusCode());
        try {
            final Transaction saved = transactionService.save(transactionId, status);
            response.setStatus(Response.Status.ACCEPTED.getStatusCode());
            return new PaymentResponse<Transaction>(header, TransactionTO.from(saved).build());
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(null, e.getMessage());
        }
    }

    @RequestMapping(value="/{transactionId}", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Transaction> get(@PathVariable final long transactionId) {
        final Account loggedAccount = getLoggedAccount();
        final Transaction transaction = transactionService.get(transactionId);
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());
        if (accountHelper.owns(loggedAccount, transaction.getSource().getId())
         || accountHelper.owns(loggedAccount, transaction.getDestination().getId())) {
            return new PaymentResponse<Transaction>(header, transaction);
        }
        return this.unauthorized(null);
    }

    /**
     * retrieves an account's transactions.
     * @param accountId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PaymentResponse<List<Transaction>> getTransactions(final HttpServletResponse response,
           @RequestParam(value="reference", required=false) final String reference) {

        final Account loggedAccount = getLoggedAccount();
        final Long accountId = loggedAccount.getId();
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());
        List<Transaction> transactions = null;
        try {
            transactions = transactionService.getForAccount(accountId, reference, Transaction.Direction.BOTH);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return this.notFound(null, e.getMessage());
        }
        return new PaymentResponse<List<Transaction>>(header, transactions);
    }

}
