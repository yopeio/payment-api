/**
 *
 */
package io.yope.payment.blockchain.bitcoinj;

import org.bitcoinj.core.TransactionConfidence;

import io.yope.payment.domain.Transaction;
import io.yope.payment.exceptions.IllegalTransactionStateException;
import io.yope.payment.exceptions.InsufficientFundsException;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.transaction.services.TransactionStateService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mgerardi
 *
 */
@Slf4j
@AllArgsConstructor
public class ConfidenceListener implements TransactionConfidence.Listener {

    private final TransactionStateService transactionService;

    private final BlockchainSettings settings;

    @Override
    public void onConfidenceChanged(final TransactionConfidence confidence, final ChangeReason reason) {
        final String transactionHash = confidence.getTransactionHash().toString();
        final int depth = confidence.getDepthInBlocks();
        log.info("-----> Transaction {} Confidence {} - {}", transactionHash, confidence, depth);
        final Transaction loaded = transactionService.getByTransactionHash(transactionHash);
        if (loaded == null) {
            return;
        }
        if (depth >= settings.getConfirmations()) {
            try {
                if (Transaction.Status.ACCEPTED.equals(loaded.getStatus())) {
                    transactionService.save(loaded.getId(), loaded.toBuilder().status(Transaction.Status.COMPLETED).build());
                }
                confidence.removeEventListener(this);
            } catch (final ObjectNotFoundException e) {
                log.error("transaction not found", e);
            } catch (final InsufficientFundsException e) {
                log.error("Insufficient money", e);
            } catch (final IllegalTransactionStateException e) {
                log.error("Illegal transaction state", e);
            }
            log.info("-----> Transaction {} with id {} completed", transactionHash, loaded.getId(), transactionHash);
        }
    }
}
