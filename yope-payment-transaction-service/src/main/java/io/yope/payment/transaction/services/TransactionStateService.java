/**
 *
 */
package io.yope.payment.transaction.services;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.yope.payment.db.services.TransactionDbService;
import io.yope.payment.db.services.WalletDbService;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.IllegalTransactionStateException;
import io.yope.payment.exceptions.InsufficientFundsException;
import io.yope.payment.exceptions.ObjectNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author massi
 *
 */
@Slf4j
@Service
public class TransactionStateService {

    @Autowired
    private TransactionDbService transactionService;

    @Autowired
    private WalletDbService walletService;

    public Transaction getByTransactionHash(final String hash) {
        return transactionService.getByTransactionHash(hash);
    }

    public Transaction getByReceiverHash(final String hash) {
        return transactionService.getByReceiverHash(hash);
    }

    public Transaction save(final Long id, final Transaction transaction) throws ObjectNotFoundException, InsufficientFundsException, IllegalTransactionStateException{
        final Transaction current = transactionService.get(id);
        if (current == null) {
            throw new ObjectNotFoundException(MessageFormat.format("Transaction with id {0} Not Found", id));
        }
        final Transaction next = transaction.toBuilder()
                .id(id)
                .source(current.getSource())
                .destination(transaction.getDestination())
                .amount(transaction.getAmount())
                .fees(transaction.getFees())
                .creationDate(transaction.getCreationDate())
                .acceptedDate(transaction.getAcceptedDate())
                .completedDate(transaction.getCompletedDate())
                .failedDate(transaction.getFailedDate())
                .deniedDate(transaction.getDeniedDate())
                .transactionHash(transaction.getTransactionHash())
                .type(transaction.getType())
                .build();
        return doSave(current, next);
    }

    /**
     * actions:
     * from PENDING
     *     -> ACCEPTED - transfer balance
     *     -> DENIED|FAILED|EXPIRED - do nothing
     * from ACCEPTED
     *     -> COMPLETED - transfer availableBalance
     *     -> FAILED|EXPIRED restore balance
     * from COMPLETED |FAILED|EXPIRED
     *     -> DENIED
     * @param current the existing transaction
     * @param next the new transaction
     * @return an updated transaction
     * @throws ObjectNotFoundException
     * @throws InsufficientFundsException
     * @throws IllegalTransactionStateException
     */
    private Transaction doSave(final Transaction current, final Transaction next) throws ObjectNotFoundException, InsufficientFundsException, IllegalTransactionStateException {
        final Transaction.Builder transaction = next.toBuilder();
        final Long now = System.currentTimeMillis();
        if (!current.getStatus().equals(next.getStatus())) {
            checkStatus(current.getStatus(), next.getStatus());
            switch (current.getStatus()) {
                case PENDING:
                    if (Status.ACCEPTED.equals(next.getStatus())) {
                        transaction.acceptedDate(now);
                        updateBalance(current);
                    } else if (Status.DENIED.equals(next.getStatus()) ||
                            Status.FAILED.equals(next.getStatus()) ||
                            Status.EXPIRED.equals(next.getStatus())) {
                        transaction.failedDate(now).deniedDate(now).expiredDate(now);
                    }
                    break;
                case ACCEPTED:
                    if (Status.COMPLETED.equals(next.getStatus())) {
                        transaction.completedDate(now);
                        updateAvailableBalance(current);
                    } else if (Status.FAILED.equals(next.getStatus()) || Status.EXPIRED.equals(next.getStatus())) {
                        transaction.failedDate(now).expiredDate(now);
                        restoreBalance(current);
                    }
                    break;
                default:
                    break;
            }
        }
        transaction.amount(current.getAmount()).id(current.getId()).type(current.getType()).source(current.getSource()).destination(current.getDestination());
        return transactionService.save(current.getId(), transaction.build());

    }

    /**
     * check valid transition according to following rules:
     * PENDING   -> DENIED - ACCEPTED - FAILED - EXPIRED - !COMPLETED
     * ACCEPTED  -> COMPLETED - FAILED - EXPIRED
     * DENIED    -> none
     * COMPLETED -> none
     * FAILED    -> none
     * EXPIRED   -> none
     *
     * @param next
     *            the next status
     * @param current
     *            the current status
     * @throws IllegalTransactionStateException
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
                break;
        }
        throw new IllegalTransactionStateException().from(current).to(next);
    }

    private void updateBalance(final Transaction transaction)
            throws ObjectNotFoundException, InsufficientFundsException {
        final Wallet source = transaction.getSource();
        if (source.getAvailableBalance().floatValue() < transaction.getAmount().floatValue()) {
            throw new InsufficientFundsException("not enough funds to accept transaction '"+transaction+"'");
        }
        final Wallet destination = transaction.getDestination();
        log.info("** balance from {}:{} to {}:{} -> amount {}", source.getName(), source.getBalance(), destination.getName(), destination.getBalance(), transaction.getAmount());
        final Wallet nextSource = walletService.save(source.getId(), source.toBuilder()
                .balance(source.getBalance().subtract(transaction.getAmount()))
                .build());
        final Wallet nextDestination = walletService.save(destination.getId(), destination.toBuilder()
                .balance(destination.getBalance().add(transaction.getAmount()))
                .build());
        log.info("** new balance  {}:{}  {}:{} ", nextSource.getName(), nextSource.getBalance(), nextDestination.getName(), nextDestination.getBalance());
    }

    private void updateAvailableBalance(final Transaction transaction)
            throws ObjectNotFoundException, InsufficientFundsException {
        final Wallet source = transaction.getSource();
        if (source.getAvailableBalance().floatValue() < transaction.getAmount().floatValue()) {
            throw new InsufficientFundsException("not enough funds to complete transaction '"+transaction+"'");
        }
        final Wallet destination = transaction.getDestination();
        log.info("** Available Balance from {}:{} to {}:{} -> amount {}", source.getName(), source.getBalance(), destination.getName(), destination.getBalance(), transaction.getAmount());
        final Wallet nextSource = walletService.save(source.getId(), source.toBuilder()
                .availableBalance(source.getAvailableBalance().subtract(transaction.getAmount()))
                .build());
        final Wallet nextDestination = walletService.save(destination.getId(), destination.toBuilder()
                .availableBalance(destination.getAvailableBalance().add(transaction.getAmount()))
                .build());
        log.info("** new Available Balance  {}:{}  {}:{} ", nextSource.getName(), nextSource.getBalance(), nextDestination.getName(), nextDestination.getBalance());
    }

    private void restoreBalance(final Transaction transaction)
            throws ObjectNotFoundException, InsufficientFundsException {
        final Wallet source = transaction.getSource();
        final Wallet destination = transaction.getDestination();
        if (destination.getBalance().compareTo(transaction.getAmount()) < 0) {
            throw new InsufficientFundsException("not enough funds to restore transaction '"+transaction+"'");
        }
        log.info("-- restore balance from {}:{} to {}:{} -> amount {}", source.getName(), source.getBalance(), destination.getName(), destination.getBalance(), transaction.getAmount());
        final Wallet nextSource = walletService.save(source.getId(), source.toBuilder()
                .balance(source.getBalance().add(transaction.getAmount()))
                .build());
        final Wallet nextDestination = walletService.save(destination.getId(), destination.toBuilder()
                .balance(destination.getBalance().subtract(transaction.getAmount()))
                .build());
        log.info("-- new balance  {}:{}  {}:{} ", nextSource.getName(), nextSource.getBalance(), nextDestination.getName(), nextDestination.getBalance());
    }



}