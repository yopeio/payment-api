/**
 *
 */
package io.yope.payment.schedulers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.yope.payment.db.services.TransactionDbService;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.exceptions.IllegalTransactionStateException;
import io.yope.payment.exceptions.InsufficientFundsException;
import io.yope.payment.exceptions.ObjectNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author massi
 *
 */
@Component
@Slf4j
public class TransactionCleanerTask {

    private static final int MINUTE = 60 * 1000;

    private static final int HOUR = 60 * MINUTE;

    /**
     * the delay for the scheduler.
     * currently 30 minutes.
     */
    private static final int PENDING_DELAY = 30 * MINUTE;

    /**
     * the delay for the scheduler.
     * currently 2 hours.
     */
    private static final int ACCEPTED_DELAY = 2 * HOUR;

    @Autowired
    private TransactionDbService transactionService;

    /**
     *
     */
    @Scheduled(fixedDelay = PENDING_DELAY)
    public void purgePendingTransactions() {
        purgeTransactions(Status.PENDING);
    }

    @Scheduled(fixedDelay = ACCEPTED_DELAY)
    public void purgeAcceptedTransactions() {
        purgeTransactions(Status.ACCEPTED);
    }

    private void purgeTransactions(final Status status) {
        final List<Transaction> transactions = transactionService.getTransaction(PENDING_DELAY, status);
        log.info("cleaning {} {} transactions", transactions.size(), status);
        for (final Transaction transaction : transactions) {
            try {
                transactionService.save(transaction.getId(),
                        transaction.toBuilder().receiverHash(null).QR(null).status(Status.EXPIRED).build());
            } catch (ObjectNotFoundException | InsufficientFundsException | IllegalTransactionStateException e) {
                log.error("Failed to save transaction {}", transaction);
                log.error("Error", e);
            }
        }
    }

}