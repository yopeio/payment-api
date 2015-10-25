/**
 *
 */
package io.yope.payment.neo4j.services;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import io.yope.payment.db.services.TransactionDbService;
import io.yope.payment.db.services.WalletDbService;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Direction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.domain.Transaction.Type;
import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.IllegalTransactionStateException;
import io.yope.payment.exceptions.InsufficientFundsException;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.neo4j.domain.Neo4JTransaction;
import io.yope.payment.neo4j.repositories.TransactionRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * @author massi
 *
 */
@Service
@Transactional
@Slf4j
public class Neo4JTransactionService implements TransactionDbService {

    @Autowired
    private WalletDbService walletService;

    @Autowired
    private TransactionRepository repository;

    /*
     * (non-Javadoc)
     *
     * @see
     * io.yope.payment.services.TransactionService#create(io.yope.payment.domain
     * .Transaction)
     */
    @Override
    public Transaction create(final Transaction transaction) throws ObjectNotFoundException {
        final Transaction toSave = createTransaction(transaction);
        return repository.save(Neo4JTransaction.from(toSave).build()).toTransaction();
    }

    @Override
    public Transaction save(final Long transactionId, final Transaction transaction) throws ObjectNotFoundException, InsufficientFundsException, IllegalTransactionStateException {
        if (!repository.exists(transactionId)) {
            throw new ObjectNotFoundException(MessageFormat.format("Transaction with id {0} Not Found", transactionId));
        }
        return repository.save(Neo4JTransaction.from(transaction).build()).toTransaction();
    }

    private Transaction createTransaction(final Transaction transaction) throws ObjectNotFoundException {
        final Wallet source = walletService.getById(transaction.getSource().getId());
        final Wallet destination = walletService.getById(transaction.getDestination().getId());
        final StringBuffer msg = new StringBuffer();
        if (source == null || destination == null) {
            if (source == null) {
                msg.append("source with hash:").append(transaction.getSource().getWalletHash()).append(" not found\n");
            }
            if (destination == null) {
                msg.append("destination with hash:").append(transaction.getDestination().getWalletHash())
                        .append(" not found\n");
            }
            throw new ObjectNotFoundException(msg.toString());
        }
        return transaction.toBuilder().source(source).destination(destination)
                .creationDate(System.currentTimeMillis()).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see io.yope.payment.services.TransactionService#get(java.lang.Long)
     */
    @Override
    public Transaction get(final Long id) {
        final Neo4JTransaction transaction = repository.findOne(id);
        return transaction == null? null : transaction.toTransaction();
    }

    /*
     * (non-Javadoc)
     *
     * @see io.yope.payment.services.TransactionService#getForWallet(java.lang.
     * String, java.lang.String, io.yope.payment.domain.Transaction.Direction)
     */
    @Override
    public List<Transaction> getForWallet(final Long walledId, final String reference, final Direction direction, final Status status, final Type type)
            throws ObjectNotFoundException {
        final String referenceParam = StringUtils.defaultIfBlank(reference, ".*");
        final String statusParam = StringUtils.defaultIfBlank(status==null?null:status.name(), ".*");
        final String typeParam = StringUtils.defaultIfBlank(type==null?null:type.name(), ".*");
        switch (direction) {
            case IN:
                return Lists.newArrayList(repository.findWalletTransactionsIn(walledId, referenceParam, statusParam, typeParam)).stream().map(t -> t.toTransaction()).collect(Collectors.toList());
            case OUT:
                return Lists.newArrayList(repository.findWalletTransactionsOut(walledId, referenceParam, statusParam, typeParam)).stream().map(t -> t.toTransaction()).collect(Collectors.toList());
            default:
                break;
        }
        return Lists.newArrayList(repository.findWalletTransactions(walledId, referenceParam, statusParam, typeParam)).stream().map(t -> t.toTransaction()).collect(Collectors.toList());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * io.yope.payment.services.TransactionService#getForAccount(java.lang.Long,
     * java.lang.String, io.yope.payment.domain.Transaction.Direction)
     */
    @Override
    public List<Transaction> getForAccount(final Long accountId, final String reference,
            final Direction direction, final Status status, final Type type)
            throws ObjectNotFoundException {
        final String referenceParam = StringUtils.defaultIfBlank(reference, ".*");
        final String statusParam = StringUtils.defaultIfBlank(status==null?null:status.name(), ".*");
        final String typeParam = StringUtils.defaultIfBlank(type==null?null:type.name(), ".*");
        switch (direction) {
            case IN:
                return Lists.newArrayList(repository.findAccountTransactionsIn(accountId, referenceParam, statusParam, typeParam)).stream().map(t -> t.toTransaction()).collect(Collectors.toList());
            case OUT:
                return Lists.newArrayList(repository.findAccountTransactionsOut(accountId, referenceParam, statusParam, typeParam)).stream().map(t -> t.toTransaction()).collect(Collectors.toList());
            default:
                break;
        }
        return Lists.newArrayList(repository.findAccountTransactions(accountId, referenceParam, statusParam, typeParam)).stream().map(t -> t.toTransaction()).collect(Collectors.toList());
    }

    @Override
    public Transaction getBySenderHash(final String hash) {
        final Neo4JTransaction transaction = repository.findBySenderHash(hash);
        return transaction == null? null : transaction.toTransaction();

    }

    @Override
    public Transaction getByReceiverHash(final String hash) {
        final Neo4JTransaction transaction = repository.findByReceiverHash(hash);
        return transaction == null? null : transaction.toTransaction();

    }

    @Override
    public Transaction getByTransactionHash(final String hash) {
        final Neo4JTransaction transaction = repository.findByTransactionHash(hash);
        return transaction == null? null : transaction.toTransaction();

    }

    @Override
    public List<Transaction> getTransaction(final int delay, final Transaction.Status status) {
        return Lists.newArrayList(repository.findOlderThan(delay, status.name())).stream().map(t -> t.toTransaction()).collect(Collectors.toList());
    }

}