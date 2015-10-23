/**
 *
 */
package io.yope.payment.neo4j.services;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

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
import io.yope.payment.services.TransactionService;
import io.yope.payment.services.WalletService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author massi
 *
 */
@Service
@Transactional
@Slf4j
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
        final Transaction toSave = createTransaction(transaction);
        return repository.save(Neo4JTransaction.from(toSave).build()).toTransaction();
    }

    @Override
    public Transaction save(final Long transactionId, final Transaction next) throws ObjectNotFoundException, InsufficientFundsException, IllegalTransactionStateException {
        if (!repository.exists(transactionId)) {
            throw new ObjectNotFoundException("No transaction with id " + transactionId, Transaction.class);
        }
        final Transaction current = repository.findOne(transactionId).toTransaction();
        return doSave(current, next);
    }

    @Override
    public Transaction transition(final Long transactionId, final Status status) throws ObjectNotFoundException, InsufficientFundsException, IllegalTransactionStateException {
        if (!repository.exists(transactionId)) {
            throw new ObjectNotFoundException(MessageFormat.format("transaction with id {} not found", transactionId),
                    Transaction.class);
        }
        final Transaction current = repository.findOne(transactionId).toTransaction();
        final Transaction next = current.toBuilder().status(status).build();
        return doSave(current, next);
    }

    private Transaction doSave(final Transaction current, final Transaction transaction) throws ObjectNotFoundException, InsufficientFundsException, IllegalTransactionStateException {
        final Transaction.Builder next = transaction.toBuilder();
        if (!current.getStatus().equals(transaction.getStatus())) {
            checkStatus(current.getStatus(), transaction.getStatus());
            switch (transaction.getStatus()) {
                case DENIED:
                    next.deniedDate(System.currentTimeMillis());
                    makeFundsAvailable(current.getSource(), current.getAmount());
                    break;
                case FAILED:
                    next.failedDate(System.currentTimeMillis());
                    makeFundsAvailable(current.getSource(), current.getAmount());
                    if (Transaction.Status.ACCEPTED.equals(current.getStatus())) {
                        restoreBalance(current.getDestination(), transaction.getBalance());
                    }
                    break;
                case EXPIRED:
                    next.expiredDate(System.currentTimeMillis());
                    makeFundsAvailable(current.getSource(), current.getAmount());
                    if (Transaction.Status.ACCEPTED.equals(current.getStatus())) {
                        restoreBalance(current.getDestination(), transaction.getBalance());
                    }
                    break;
                case ACCEPTED:
                    next.acceptedDate(System.currentTimeMillis());
                    transferFunds(current.getSource(), current.getDestination(), transaction.getAmount(), transaction.getBalance(), transaction.getReference());
                    break;
                case COMPLETED:
                    next.completedDate(System.currentTimeMillis());
                    makeFundsAvailable(current.getDestination(), transaction.getBalance());
                    break;
                default:
                    break;
            }
        }
        final Transaction toSave = next.amount(current.getAmount()).id(current.getId()).type(current.getType()).source(current.getSource()).destination(current.getDestination()).build();
        return repository.save(Neo4JTransaction.from(toSave).build()).toTransaction();

    }

    /**
     * check valid transition according to following rules:
     * PENDING -> DENIED - ACCEPTED - FAILED - EXPIRED - !COMPLETED
     * ACCEPTED -> COMPLETED - FAILED - EXPIRED
     * DENIED -> none
     * COMPLETED -> none
     * FAILED -> none
     * EXPIRED -> none
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
                if (Status.COMPLETED.equals(next) ||
                    Status.EXPIRED.equals(next) ||
                    Status.FAILED.equals(next)) {
                    return;
                }
                break;
            default:
                throw new IllegalTransactionStateException(current, next);
        }
    }

    private void transferFunds(final Wallet source, final Wallet destination, final BigDecimal amount, final BigDecimal balance, final String transaction) throws ObjectNotFoundException, InsufficientFundsException {
        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("not enough funds to complete transaction '"+transaction+"'");
        }
        log.info("** transfer funds from {}:{} to {}:{} -> amount {} balance {}", source.getName(), source.getBalance(), destination.getName(), destination.getBalance(), amount, balance);
        final Wallet newSource = walletService.update(source.getId(), source.toBuilder()
                .balance(source.getBalance().subtract(amount))
                .build());
        final Wallet newDestination = walletService.update(destination.getId(), destination.toBuilder()
                .balance(destination.getBalance().add(balance))
                .build());
        log.info("** new funds {}:{}  {}:{} ", newSource.getName(), newSource.getBalance(), newDestination.getName(), newDestination.getBalance());
    }

    private void makeFundsAvailable(final Wallet wallet, final BigDecimal amount) throws ObjectNotFoundException {
        log.info("** add available funds to {}:{} -> {}", wallet.getName(), wallet.getAvailableBalance(), amount);
        final Wallet newWallet = walletService.update(wallet.getId(), wallet.toBuilder()
                .availableBalance(wallet.getAvailableBalance().add(amount))
                .build());
        log.info("** available funds to {}:{}", newWallet.getName(), newWallet.getAvailableBalance());
    }

    private void restoreBalance(final Wallet wallet, final BigDecimal balance) throws ObjectNotFoundException {
        log.info("** restore balance to {}:{} -> {}", wallet.getName(), wallet.getBalance(), balance);
        final Wallet newWallet = walletService.update(wallet.getId(), wallet.toBuilder()
                .balance(wallet.getBalance().subtract(balance))
                .build());
        log.info("** restored balance to {}:{}", newWallet.getName(), newWallet.getBalance());
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
            throw new ObjectNotFoundException(msg.toString(), Wallet.class);
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
