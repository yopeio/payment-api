package io.yope.payment.rest.resources;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.transferobjects.TransactionTO;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.TransactionService;

/**
 * Wallet Resource.
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/transactions")
public class TransactionResource {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    /**
     * Create Wallet.
     * @param transaction
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Transaction> createTransaction(
            final HttpServletResponse response,
            @RequestBody(required=false) final TransactionTO transaction) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.CREATED.getStatusCode());
        final TransactionTO toSave =  TransactionTO.builder()
                .acceptedDate(transaction.getAcceptedDate())
                .amount(transaction.getAmount())
                .completedDate(transaction.getCompletedDate())
                .creationDate(transaction.getCreationDate())
                .deniedDate(transaction.getDeniedDate())
                .description(transaction.getDescription())
                .destination(transaction.getDestination())
                .reference(transaction.getReference())
                .source(transaction.getSource())
                .status(transaction.getStatus())
                .build();
        try {
            final Transaction saved = transactionService.create(toSave);
            response.setStatus(Response.Status.CREATED.getStatusCode());
            return new PaymentResponse<Transaction>(header, saved);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return new PaymentResponse<Transaction>(header.success(false).errorCode("not found"), null);
        }
    }

    @RequestMapping(value="/{transactionId}", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Transaction> get(@PathVariable final long transactionId) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());
        return new PaymentResponse<Transaction>(header, transactionService.get(transactionId));
    }

    /**
     * retrieves an account's transactions.
     * @param accountId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PaymentResponse<List<Transaction>> getTransactions(final HttpServletResponse response,
                                                                                   @RequestHeader final String reference,
                                                                                   @RequestHeader final long accountId) {
        accountService.getById(accountId);
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());
        List<Transaction> transactions = null;
        try {
            transactions = transactionService.getForAccount(accountId, reference, Transaction.Direction.BOTH);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return new PaymentResponse<List<Transaction>>(header.success(false).errorCode(e.getMessage()), null);
        }
        return new PaymentResponse<List<Transaction>>(header, transactions);
    }

}
