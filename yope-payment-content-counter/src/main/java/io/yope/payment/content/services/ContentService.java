/**
 *
 */
package io.yope.payment.content.services;

import org.springframework.beans.factory.annotation.Autowired;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.TransactionService;
import io.yope.payment.services.WalletService;

/**
 * @author mgerardi
 *
 */
public class ContentService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;

    public boolean increase(final Long buyerId, final String sourceWalletName, final Long sellerId, final String destinationWalletName, final String reference) {
        final Wallet source = Wallet.from(walletService.getByName(buyerId, sourceWalletName)).build();
        if (source == null) {
            return false;
        }
        final Wallet destination = Wallet.from(walletService.getByName(sellerId, destinationWalletName)).build();
        if (destination == null) {
            return false;
        }
        final Transaction transaction = Transaction.builder().source(source).destination(destination).build();
        return true;
    }
}
