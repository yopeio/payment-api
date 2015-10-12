/**
 *
 */
package io.yope.payment.rest.helpers;

import org.springframework.beans.factory.annotation.Autowired;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.blockchain.BlockchainException;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.services.TransactionService;

/**
 * @author massi
 *
 */
public class TransactionHelper {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BlockChainService blockChainService;

    public Transaction register(final Transaction transaction) throws BlockchainException, ObjectNotFoundException {
        final Transaction pendingTransaction = transactionService.create(transaction);
        if (Transaction.Type.EXTERNAL.equals(pendingTransaction.getType())) {
            blockChainService.send(pendingTransaction);
        }
        try {
            return transactionService.save(pendingTransaction.getId(), Status.ACCEPTED);
        } catch (final Exception e) {
        }
        return pendingTransaction;
    }



}
