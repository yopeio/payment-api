package io.yope.payment.rest.resources;

import java.util.List;
import java.util.UUID;

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
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.WalletTO;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.services.TransactionService;
import io.yope.payment.services.WalletService;

/**
 * Wallet Resource.
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/wallets")
public class WalletResource {

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;


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
        final WalletTO toSave =  WalletTO.builder().
                name(wallet.getName()).
                status(Wallet.Status.PENDENT).
                type(wallet.getType()).
                balance(wallet.getBalance()).
                description(wallet.getDescription())
                .hash(UUID.randomUUID().toString())
                .build();
        response.setStatus(Response.Status.CREATED.getStatusCode());
        return new PaymentResponse<Wallet>(header, walletService.create(toSave));
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
        Wallet saved = null;
        try {
            saved = walletService.update(wallet.getId(), wallet);
            return new PaymentResponse<Wallet>(header, saved);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return new PaymentResponse<Wallet>(header.success(false).errorCode(e.getMessage()), wallet);
        }
    }

    /**
     * Get Wallets by Account ID.
     * @param accountId
     * @param response
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<List<Wallet>> getWallets(@RequestHeader final long accountId,
                                                                  final HttpServletResponse response) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());
        final List<Wallet> wallets = walletService.get(accountId);
        if (wallets != null && !wallets.isEmpty()) {
            return new PaymentResponse<List<Wallet>>(header, wallets);
        } else {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return new PaymentResponse<List<Wallet>>(header.success(false).errorCode("not found"), wallets);
        }
    }

    /**
     * Get Wallet By ID.
     * @param accountId
     * @param walletId
     * @param response
     * @return
     */
    @RequestMapping(value="/{walletId}", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public PaymentResponse<Wallet> getWallet(@RequestHeader final long accountId,
                                             @PathVariable final long walletId,
                                             final HttpServletResponse response) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());
        final Wallet wallet = walletService.getById(walletId);
        if (wallet != null) {
            return new PaymentResponse<Wallet>(header, wallet);
        } else {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return new PaymentResponse<Wallet>(header.success(false).errorCode(walletId + " not found"), wallet);
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
    public @ResponseBody PaymentResponse<List<Transaction>> getForWallet(@PathVariable final String walletId,
                                                                         @RequestHeader final String reference,
                                                                         final HttpServletResponse response) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.OK.getStatusCode());
        List<Transaction> transactions = null;
        try {
            transactions = transactionService.getForWallet(walletId, reference, Transaction.Direction.BOTH);
            return new PaymentResponse<List<Transaction>>(header, transactions);
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return new PaymentResponse<List<Transaction>>(header.success(false).errorCode("not found"), transactions);
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
    public PaymentResponse<Wallet> deactivateWallet(@RequestHeader final long accountId,
                                                    @PathVariable final long walletId,
                                                    final HttpServletResponse response) {
        final ResponseHeader header = new ResponseHeader(true, "", Response.Status.ACCEPTED.getStatusCode());
        try {
            response.setStatus(Response.Status.ACCEPTED.getStatusCode());
            return new PaymentResponse<Wallet>(header, walletService.delete(walletId));
        } catch (final ObjectNotFoundException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return new PaymentResponse<Wallet>(header.success(false).errorCode(walletId + " not found"), null);
        }
    }

}
