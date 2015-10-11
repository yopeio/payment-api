/**
 *
 */
package io.yope.payment.neo4j.services;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Direction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.neo4j.domain.Neo4JTransaction;
import io.yope.payment.neo4j.domain.Neo4JWallet;
import io.yope.payment.neo4j.repositories.TransactionRepository;
import io.yope.payment.services.TransactionService;
import io.yope.payment.services.WalletService;

/**
 * @author massi
 *
 */
@Service
@Transactional
public class Neo4JTransactionService implements TransactionService {

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionRepository repository;


    /* (non-Javadoc)
     * @see io.yope.payment.services.TransactionService#create(io.yope.payment.domain.Transaction)
     */
    @Override
    public Transaction create(final Transaction transaction) throws ObjectNotFoundException {
        final Neo4JTransaction toSave = createTransaction(transaction);
        return repository.save(toSave);
    }

    @Override
    public Transaction save(final Long transactionId, final Status status) throws ObjectNotFoundException {
        final Transaction transaction = get(transactionId);
        if (transaction == null) {
            throw new ObjectNotFoundException(MessageFormat.format("transaction with id {} not found", transactionId), Transaction.class);
        }
        final Neo4JTransaction.Neo4JTransactionBuilder toSave = Neo4JTransaction.from(transaction);
        switch (status) {
            case ACCEPTED:
                toSave.status(status).acceptedDate(System.currentTimeMillis());
                break;
            case DENIED:
                toSave.status(status).deniedDate(System.currentTimeMillis());
                break;
            case COMPLETED:
                toSave.status(status).completedDate(System.currentTimeMillis());
                break;
            default:
                break;
        }

        return repository.save(toSave.build());

    }


    private Neo4JTransaction createTransaction(final Transaction transaction) throws ObjectNotFoundException {
        final Neo4JWallet source = Neo4JWallet.from(walletService.getById(transaction.getSource().getId())).build();
        final Neo4JWallet destination = Neo4JWallet.from(walletService.getById(transaction.getDestination().getId())).build();
        final StringBuffer msg = new StringBuffer();
        if (source == null || destination == null) {
            if (source == null) {
                msg.append("source with hash:").append(transaction.getSource().getHash()).append(" not found\n");
            }
            if (destination == null) {
                msg.append("destination with hash:").append(transaction.getDestination().getHash()).append(" not found\n");
            }
            throw new ObjectNotFoundException(msg.toString(), Wallet.class);
        }
        return Neo4JTransaction.from(transaction)
                .source(source)
                .destination(destination)
                .status(Status.PENDING)
                .creationDate(System.currentTimeMillis())
                .build();
    }

    /* (non-Javadoc)
     * @see io.yope.payment.services.TransactionService#get(java.lang.Long)
     */
    @Override
    public Transaction get(final Long id) {
        return repository.findOne(id);
    }

    /* (non-Javadoc)
     * @see io.yope.payment.services.TransactionService#getForWallet(java.lang.String, java.lang.String, io.yope.payment.domain.Transaction.Direction)
     */
    @Override
    public List<Transaction> getForWallet(final String walletHash, final String reference, final Direction direction)
            throws ObjectNotFoundException {
        if (Direction.IN.equals(direction)) {
            if (Strings.isNullOrEmpty(reference)) {
                return new ArrayList<Transaction>(repository.findWalletTransactionsIn(walletHash));
            }
            return new ArrayList<Transaction>(repository.findWalletTransactionsIn(walletHash, reference));
        }
        if (Direction.OUT.equals(direction)) {
            if (Strings.isNullOrEmpty(reference)) {
                return new ArrayList<Transaction>(repository.findWalletTransactionsOut(walletHash));
            }
            return new ArrayList<Transaction>(repository.findWalletTransactionsOut(walletHash, reference));
        }
        if (Strings.isNullOrEmpty(reference)) {
            return new ArrayList<Transaction>(repository.findWalletTransactions(walletHash));
        }
        return new ArrayList<Transaction>(repository.findWalletTransactions(walletHash, reference));
    }

    /* (non-Javadoc)
     * @see io.yope.payment.services.TransactionService#getForAccount(java.lang.Long, java.lang.String, io.yope.payment.domain.Transaction.Direction)
     */
    @Override
    public List<Transaction> getForAccount(final Long accountId, final String reference, final Direction direction)
            throws ObjectNotFoundException {
        if (Direction.IN.equals(direction)) {
            if (Strings.isNullOrEmpty(reference)) {
                return new ArrayList<Transaction>(repository.findAccountTransactionsIn(accountId));
            }
            return new ArrayList<Transaction>(repository.findAccountTransactionsIn(accountId, reference));
        }
        if (Direction.OUT.equals(direction)) {
            if (Strings.isNullOrEmpty(reference)) {
                return new ArrayList<Transaction>(repository.findAccountTransactionsOut(accountId));
            }
            return new ArrayList<Transaction>(repository.findAccountTransactionsOut(accountId, reference));
        }
        if (Strings.isNullOrEmpty(reference)) {
            return new ArrayList<Transaction>(repository.findAccountTransactions(accountId));
        }
        return new ArrayList<Transaction>(repository.findAccountTransactions(accountId, reference));
    }

}
