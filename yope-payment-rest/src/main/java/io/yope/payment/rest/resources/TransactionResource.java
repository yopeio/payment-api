package io.yope.payment.rest.resources;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.TransactionTO;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Wallet Resource.
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/transactions")
@Slf4j
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
    public @ResponseBody PaymentResponse<Wallet> createWallet(
            final HttpServletResponse response,
            @RequestBody(required=false) final TransactionTO transaction) {
        final ResponseHeader header = new ResponseHeader(true, "");
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
        Transaction saved = null;
        try {
            saved = transactionService.create(toSave);
            response.setStatus(Response.Status.CREATED.getStatusCode());
            return new PaymentResponse(header, saved);
        } catch (ObjectNotFoundException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return new PaymentResponse(header.success(false).errorCode("not found"), saved);
        }
    }

    @RequestMapping(value="/{transactionId}", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Transaction> get(@PathVariable final long transactionId) {
        final ResponseHeader header = new ResponseHeader(true, "");
        return new PaymentResponse(header, transactionService.get(transactionId));
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
        Account account = accountService.getById(accountId);
        final ResponseHeader header = new ResponseHeader(true, "");
        List<Transaction> transactions = null;
        try {
            transactions = transactionService.getForAccount(accountId, reference, Transaction.Direction.BOTH);
        } catch (ObjectNotFoundException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return new PaymentResponse(header.success(false).errorCode(e.getMessage()), account);
        }
        return new PaymentResponse<List<Transaction>>(header, transactions);
    }

}
