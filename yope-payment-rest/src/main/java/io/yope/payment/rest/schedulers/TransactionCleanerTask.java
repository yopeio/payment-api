/**
 *
 */
package io.yope.payment.rest.schedulers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.domain.transferobjects.TransactionTO;
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

    /**
     * the delay for the scheduler.
     * currently 30 minutes.
     */
    private static final int DELAY = 900000;

    @Autowired
    private TransactionService transactionService;

    /**
     *
     */
    @Scheduled(fixedDelay = DELAY)
    public void purgeTransaction() {
        log.info("cleaning expired transactions");
        final List<Transaction> transactions = transactionService.getTransaction(DELAY, Status.PENDING);
        for (final Transaction transaction : transactions) {
            try {
                transactionService.save(transaction.getId(), TransactionTO.from(transaction).status(Status.EXPIRED).build());
            } catch (ObjectNotFoundException | InsufficientFundsException | IllegalTransactionStateException e) {

            }
        }
        log.info("cleaned {} expired transactions", transactions.size());
    }
}
