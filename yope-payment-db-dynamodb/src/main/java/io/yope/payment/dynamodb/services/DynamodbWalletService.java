/**
 *
 */
package io.yope.payment.dynamodb.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.yope.payment.db.services.AccountDbService;
import io.yope.payment.db.services.WalletDbService;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.Wallet.Status;
import io.yope.payment.dynamodb.domain.DynamodbWallet;
import io.yope.payment.dynamodb.repositories.WalletRepository;
import io.yope.payment.exceptions.ObjectNotFoundException;

/**
 * @author mgerardi
 *
 */
@Service
public class DynamodbWalletService implements WalletDbService {

    @Autowired
    private WalletRepository repository;

    private AccountDbService accountService;

    @Override
    public Wallet create(final Wallet wallet) {
        return this.repository.save(DynamodbWallet.from(wallet).build()).toWallet();
    }

    @Override
    public boolean exists(final Long id) {
        return this.repository.exists(id);
    }

    @Override
    public Wallet getById(final Long id) {
        final DynamodbWallet wallet = this.repository.findOne(id);
        if (wallet == null) {
            return null;
        }
        return wallet.toWallet();
    }

    @Override
    public Wallet getByWalletHash(final String hash) {
        final DynamodbWallet wallet = this.repository.findByWalletHash(hash);
        if (wallet == null) {
            return null;
        }
        return wallet.toWallet();
    }

    @Override
    public Wallet getByName(final Long accountId, final String name) {
        final DynamodbWallet wallet = this.repository.findByName(name);
        if (wallet == null) {
            return null;
        }
        return wallet.toWallet();
    }

    @Override
    public Wallet save(final Long id, final Wallet wallet) throws ObjectNotFoundException {
        if (!this.exists(id)) {
            throw new ObjectNotFoundException(id, Wallet.class);
        }
        return this.repository.save(DynamodbWallet.from(wallet).id(id).modificationDate(System.currentTimeMillis()).build()).toWallet();
    }

    @Override
    public Wallet delete(final Long id) throws ObjectNotFoundException {
        if (!this.exists(id)) {
            throw new ObjectNotFoundException(id, Wallet.class);
        }
        final DynamodbWallet wallet = this.repository.findOne(id);
        return this.repository.save(wallet.withStatus(Status.DELETED).withModificationDate(System.currentTimeMillis())).toWallet();
    }

    @Override
    public List<Wallet> getWalletsByAccountId(final Long accountId, final Status status) {
        return this.accountService.getById(accountId).getWallets()
                .stream()
                .filter(it -> status == null || status.equals(it.getStatus()))
                .collect(Collectors.toList());
    }



}
