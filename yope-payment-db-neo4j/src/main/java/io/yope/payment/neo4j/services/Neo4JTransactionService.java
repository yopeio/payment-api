/**
 *
 */
package io.yope.payment.neo4j.services;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Direction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.domain.Transaction.Type;
import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.IllegalTransactionStateException;
import io.yope.payment.exceptions.InsufficientFundsException;
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

    /*
     * (non-Javadoc)
     *
     * @see
     * io.yope.payment.services.TransactionService#create(io.yope.payment.domain
     * .Transaction)
     */
    @Override
    public Transaction create(final Transaction transaction) throws ObjectNotFoundException {
        final Neo4JTransaction toSave = createTransaction(transaction);
        return repository.save(toSave);
    }

    @Override
    public Transaction save(final Long transactionId, final Transaction transaction) throws ObjectNotFoundException, InsufficientFundsException, IllegalTransactionStateException {
        if (!repository.exists(transactionId)) {
            throw new ObjectNotFoundException("No transaction with id " + transactionId, Transaction.class);
        }
        final Neo4JTransaction current = repository.findOne(transactionId);
        final Neo4JTransaction next = Neo4JTransaction.from(transaction).build();
        return doSave(current, next);
    }

    @Override
    public Transaction transition(final Long transactionId, final Status status) throws ObjectNotFoundException, InsufficientFundsException, IllegalTransactionStateException {
        if (!repository.exists(transactionId)) {
            throw new ObjectNotFoundException(MessageFormat.format("transaction with id {} not found", transactionId),
                    Transaction.class);
        }
        final Neo4JTransaction current = repository.findOne(transactionId);
        final Neo4JTransaction next = Neo4JTransaction.from(current).status(status).build();
        return doSave(current, next);
    }

    private Transaction doSave(final Neo4JTransaction current, final Neo4JTransaction transaction) throws ObjectNotFoundException, InsufficientFundsException, IllegalTransactionStateException {
        final Neo4JTransaction.Neo4JTransactionBuilder next = Neo4JTransaction.from(transaction);
        if (!current.getStatus().equals(transaction.getStatus())) {
            checkStatus(current.getStatus(), transaction.getStatus());
            switch (transaction.getStatus()) {
                case ACCEPTED:
                    next.acceptedDate(System.currentTimeMillis());
                    transferFunds(current.getSource(), current.getDestination(), current.getAmount(), transaction.getReference());
                    break;
                case DENIED:
                    next.deniedDate(System.currentTimeMillis());
                    makeFundsAvailable(current.getSource(), current.getAmount());
                    break;
                case EXPIRED:
                    next.deniedDate(System.currentTimeMillis());
                    makeFundsAvailable(current.getSource(), current.getAmount());
                case COMPLETED:
                    next.completedDate(System.currentTimeMillis());
                    makeFundsAvailable(current.getDestination(), current.getAmount());
                    break;
                default:
                    break;
            }
        }

        return repository.save(next.amount(current.getAmount()).id(current.getId()).type(current.getType()).source(current.getSource()).destination(current.getDestination()).build());

    }

    /**
     * check valid transition according to following rules:
     * PENDING -> DENIED - ACCEPTED
     * ACCEPTED -> COMPLETED
     * DENIED -> none
     * COMPLETED -> none
     *
     * @param next
     *            the next status
     * @param current
     *            the current status
     * @throws IllegalTransactionStateException
     * @throws IllegalArgumentException
     *             if the check fails
     */
    private void checkStatus(final Status current, final Status next) throws IllegalTransactionStateException {
        switch (current) {
            case PENDING:
                if (!Status.COMPLETED.equals(next)) {
                    return;
                }
                break;
            case ACCEPTED:
                if (Status.COMPLETED.equals(next)) {
                    return;
                }
                break;
            default:
                throw new IllegalTransactionStateException(current, next);
        }
    }

    private void transferFunds(final Wallet source, final Wallet destination, final BigDecimal amount, final String transaction) throws ObjectNotFoundException, InsufficientFundsException {
        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("not enough funds to complete transaction '"+transaction+"'");
        }
        final Neo4JWallet newSource = Neo4JWallet.from(source)
                .balance(source.getBalance().subtract(amount))
                .build();
        walletService.update(source.getId(), newSource);
        final Neo4JWallet newDestination = Neo4JWallet
                .from(destination)
                .balance(destination.getBalance().add(amount))
                .build();
        walletService.update(destination.getId(), newDestination);
    }

    private void makeFundsAvailable(final Neo4JWallet wallet, final BigDecimal amount) throws ObjectNotFoundException {
        final Neo4JWallet newWallet = Neo4JWallet.from(wallet)
                .availableBalance(wallet.getAvailableBalance().add(amount))
                .build();
        walletService.update(wallet.getId(), newWallet);
    }

    private Neo4JTransaction createTransaction(final Transaction transaction) throws ObjectNotFoundException {
        final Neo4JWallet source = Neo4JWallet.from(walletService.getById(transaction.getSource().getId())).build();
        final Neo4JWallet destination = Neo4JWallet.from(walletService.getById(transaction.getDestination().getId()))
                .build();
        final StringBuffer msg = new StringBuffer();
        if (source == null || destination == null) {
            if (source == null) {
                msg.append("source with hash:").append(transaction.getSource().getHash()).append(" not found\n");
            }
            if (destination == null) {
                msg.append("destination with hash:").append(transaction.getDestination().getHash())
                        .append(" not found\n");
            }
            throw new ObjectNotFoundException(msg.toString(), Wallet.class);
        }
        return Neo4JTransaction.from(transaction).source(source).destination(destination)
                .creationDate(System.currentTimeMillis()).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see io.yope.payment.services.TransactionService#get(java.lang.Long)
     */
    @Override
    public Transaction get(final Long id) {
        return repository.findOne(id);
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
                return new ArrayList<Transaction>(repository.findWalletTransactionsIn(walledId, referenceParam, statusParam, typeParam));
            case OUT:
                return new ArrayList<Transaction>(repository.findWalletTransactionsOut(walledId, referenceParam, statusParam, typeParam));
            default:
                break;
        }
        return new ArrayList<Transaction>(repository.findWalletTransactions(walledId, referenceParam, statusParam, typeParam));
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
                return new ArrayList<Transaction>(repository.findAccountTransactionsIn(accountId, referenceParam, statusParam, typeParam));
            case OUT:
                return new ArrayList<Transaction>(repository.findAccountTransactionsOut(accountId, referenceParam, statusParam, typeParam));
            default:
                break;
        }
        return new ArrayList<Transaction>(repository.findAccountTransactions(accountId, referenceParam, statusParam, typeParam));
    }

    @Override
    public Transaction getForHash(final String hash) {
        return repository.findBySenderHash(hash);
    }

    @Override
    public List<Transaction> getTransaction(final int delay, final Transaction.Status status) {
        return new ArrayList<Transaction>(repository.findOlderThan(delay, status.name()));
    }

}
