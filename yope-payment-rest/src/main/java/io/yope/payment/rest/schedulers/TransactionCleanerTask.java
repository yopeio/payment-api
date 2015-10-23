/**
 *
 */
package io.yope.payment.rest.schedulers;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.exceptions.IllegalTransactionStateException;
import io.yope.payment.exceptions.InsufficientFundsException;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.services.TransactionService;
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
    private TransactionService transactionService;

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
                log.error(MessageFormat.format("Failed to save transaction {0}", transaction), e);
            }
        }
    }

}