/**
 *
 */
package io.yope.payment.dynamodb.services;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import io.yope.payment.db.services.TransactionDbService;
import io.yope.payment.db.services.WalletDbService;
import io.yope.payment.domain.Account;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Direction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.domain.Transaction.Type;
import io.yope.payment.domain.Wallet;
import io.yope.payment.dynamodb.domain.DynamodbAccount;
import io.yope.payment.dynamodb.domain.DynamodbTransaction;
import io.yope.payment.dynamodb.domain.DynamodbWallet;
import io.yope.payment.dynamodb.repositories.AccountRepository;
import io.yope.payment.dynamodb.repositories.TransactionRepository;
import io.yope.payment.exceptions.IllegalTransactionStateException;
import io.yope.payment.exceptions.InsufficientFundsException;
import io.yope.payment.exceptions.ObjectNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mgerardi
 *
 */
@Service
@Slf4j
public class DynamodbTransactionService implements TransactionDbService {

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private WalletDbService walletService;

    @Override
    public Transaction create(final Transaction transaction) throws ObjectNotFoundException {
        if (!this.walletService.exists(transaction.getSource().getId())) {
            throw new ObjectNotFoundException(transaction.getSource().getId(), Wallet.class);
        }
        if (!this.walletService.exists(transaction.getDestination().getId())) {
            throw new ObjectNotFoundException(transaction.getDestination().getId(), Wallet.class);
        }
        final DynamodbTransaction toSave = DynamodbTransaction.from(transaction)
                .sourceId(transaction.getSource().getId())
                .destinationId(transaction.getDestination().getId())
                .creationDate(System.currentTimeMillis()).build();
        return this.transformTransaction(this.repository.save(toSave));
    }

    @Override
    public Transaction save(final Long transactionId, final Transaction transaction)
            throws ObjectNotFoundException, InsufficientFundsException, IllegalTransactionStateException {
        if (this.get(transactionId) == null) {
            throw new ObjectNotFoundException(transactionId, Transaction.class);
        }
        return this.transformTransaction(this.repository.save(DynamodbTransaction.from(transaction).id(transactionId).build()));
    }

    @Override
    public Transaction get(final Long id) {
        return this.transformTransaction(this.repository.findOne(id));
    }

    @Override
    public List<Transaction> getForWallet(final Long walledId, final String reference, final Direction direction, final Status status, final Type type) throws ObjectNotFoundException {
        if (!this.walletService.exists(walledId)) {
            throw new ObjectNotFoundException(walledId, Wallet.class);
        }
        final List<DynamodbTransaction> transactions = Lists.newArrayList();
        switch (direction) {
            case IN:
                transactions.addAll(this.repository.findByDestinationId(walledId));
                break;
            case OUT:
                transactions.addAll(this.repository.findBySourceId(walledId));
                break;
         default:
             transactions.addAll(this.repository.findByDestinationId(walledId));
             transactions.addAll(this.repository.findBySourceId(walledId));
             break;
        }
        return transactions.stream()
                .filter(it -> reference == null || it.getReference().equals(reference))
                .filter(it -> status == null || it.getStatus().equals(status))
                .filter(it -> type == null || it.getType().equals(type))
                .map(it -> this.transformTransaction(it))
                .collect(Collectors.toList());

    }

    private Transaction transformTransaction(final DynamodbTransaction transaction) {
        if (transaction == null) {
            return null;
        }
        return transaction.toTransaction()
                .withDestination(this.walletService.getById(transaction.getDestinationId()))
                .withSource(this.walletService.getById(transaction.getSourceId()));
    }

    @Override
    public List<Transaction> getForAccount(final Long accountId, final String reference, final Direction direction, final Status status, final Type type) throws ObjectNotFoundException {
        final DynamodbAccount account = this.accountRepository.findOne(accountId);
        if (account == null) {
            throw new ObjectNotFoundException(accountId, Account.class);
        }
        final List<DynamodbWallet> wallets = account.getWallets();
        final List<Transaction> transactions = Lists.newArrayList();
        wallets.stream().forEach(new Consumer<DynamodbWallet>() {

            @Override
            public void accept(final DynamodbWallet it) {
                try {
                    transactions.addAll(DynamodbTransactionService.this.getForWallet(it.getId(), reference, direction, status, type));
                } catch (final ObjectNotFoundException e) {
                    log.error("for "+it.getId(), e);
                }
            }
        });
        return transactions;
    }

    @Override
    public Transaction getByReceiverHash(final String hash) {
        return this.transformTransaction( this.repository.findByReceiverHash(hash));
    }

    @Override
    public Transaction getByTransactionHash(final String hash) {
        return this.transformTransaction( this.repository.findByTransactionHash(hash));
    }

    @Override
    public Transaction getBySenderHash(final String hash) {
        return this.transformTransaction( this.repository.findBySenderHash(hash));
    }

    @Override
    public List<Transaction> getTransaction(final int delay, final Status status) {
        final Long now = System.currentTimeMillis();
        return this.repository.findByStatus(status)
                .stream()
                .filter(it -> now - it.getCreationDate() > delay)
                .map(it -> this.transformTransaction(it))
                .collect(Collectors.toList());
    }

}
