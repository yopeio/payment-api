/**
 *
 */
package io.yope.payment.neo4j.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Direction;
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

    private TransactionRepository repository;
    /* (non-Javadoc)
     * @see io.yope.payment.services.TransactionService#create(io.yope.payment.domain.Transaction)
     */
    @Override
    public Transaction create(final Transaction transaction) throws ObjectNotFoundException {
        final Neo4JTransaction toSave = this.createTransaction(transaction);
        return this.repository.save(toSave);
    }

    private Neo4JTransaction createTransaction(final Transaction transaction) throws ObjectNotFoundException {
        final Neo4JWallet source = Neo4JWallet.from(this.walletService.getByHash(transaction.getSource().getHash())).build();
        final Neo4JWallet destination = Neo4JWallet.from(this.walletService.getByHash(transaction.getDestination().getHash())).build();
        final StringBuffer msg = new StringBuffer();
        if (source == null) {
            msg.append("source hash:").append(transaction.getSource().getHash()).append("\n");
        }
        if (this.walletService.get(destination.getId())==null) {
            msg.append("destination hash:").append(transaction.getDestination().getHash()).append("\n");
        }
        if (!Strings.isNullOrEmpty(msg.toString())) {
            throw new ObjectNotFoundException(msg.toString(), Wallet.class);
        }
        return Neo4JTransaction.from(transaction)
                .source(source)
                .destination(destination)
                .creationDate(System.currentTimeMillis())
                .build();
    }

    /* (non-Javadoc)
     * @see io.yope.payment.services.TransactionService#get(java.lang.Long)
     */
    @Override
    public Transaction get(final Long id) {
        return this.repository.findOne(id);
    }

    /* (non-Javadoc)
     * @see io.yope.payment.services.TransactionService#getForWallet(java.lang.String, java.lang.String, io.yope.payment.domain.Transaction.Direction)
     */
    @Override
    public List<Transaction> getForWallet(final String walletHash, final String reference, final Direction direction)
            throws ObjectNotFoundException {
        if (Direction.IN.equals(direction)) {
            if (Strings.isNullOrEmpty(reference)) {
                return new ArrayList<Transaction>(this.repository.findWalletTransactionsIn(walletHash));
            }
            return new ArrayList<Transaction>(this.repository.findWalletTransactionsIn(walletHash, reference));
        }
        if (Direction.OUT.equals(direction)) {
            if (Strings.isNullOrEmpty(reference)) {
                return new ArrayList<Transaction>(this.repository.findWalletTransactionsOut(walletHash));
            }
            return new ArrayList<Transaction>(this.repository.findWalletTransactionsOut(walletHash, reference));
        }
        if (Strings.isNullOrEmpty(reference)) {
            return new ArrayList<Transaction>(this.repository.findWalletTransactions(walletHash));
        }
        return new ArrayList<Transaction>(this.repository.findWalletTransactions(walletHash, reference));
    }

    /* (non-Javadoc)
     * @see io.yope.payment.services.TransactionService#getForAccount(java.lang.Long, java.lang.String, io.yope.payment.domain.Transaction.Direction)
     */
    @Override
    public List<Transaction> getForAccount(final Long accountId, final String reference, final Direction direction)
            throws ObjectNotFoundException {
        if (Direction.IN.equals(direction)) {
            if (Strings.isNullOrEmpty(reference)) {
                return new ArrayList<Transaction>(this.repository.findAccountTransactionsIn(accountId));
            }
            return new ArrayList<Transaction>(this.repository.findAccountTransactionsIn(accountId, reference));
        }
        if (Direction.OUT.equals(direction)) {
            if (Strings.isNullOrEmpty(reference)) {
                return new ArrayList<Transaction>(this.repository.findAccountTransactionsOut(accountId));
            }
            return new ArrayList<Transaction>(this.repository.findAccountTransactionsOut(accountId, reference));
        }
        if (Strings.isNullOrEmpty(reference)) {
            return new ArrayList<Transaction>(this.repository.findAccountTransactions(accountId));
        }
        return new ArrayList<Transaction>(this.repository.findAccountTransactions(accountId, reference));
    }

}
