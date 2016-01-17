package io.yope.payment.dynamodb.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import io.yope.payment.db.services.AccountDbService;
import io.yope.payment.domain.Account;
import io.yope.payment.domain.Account.Status;
import io.yope.payment.domain.Account.Type;
import io.yope.payment.domain.Wallet;
import io.yope.payment.dynamodb.domain.DynamodbAccount;
import io.yope.payment.dynamodb.domain.DynamodbWallet;
import io.yope.payment.dynamodb.repositories.AccountRepository;
import io.yope.payment.dynamodb.repositories.WalletRepository;
import io.yope.payment.exceptions.ObjectNotFoundException;

public class DynamodbAccountService implements AccountDbService {

    @Autowired
    private AccountRepository repository;

    @Autowired
    private WalletRepository walletRepository;

    @Override
    public Account create(final Account account, final Wallet... wallets) {
        for (final Wallet wallet: wallets) {
            if (wallet == null) {
                continue;
            }
            account.getWallets()
                .add(this.walletRepository.save(
                        DynamodbWallet.from(wallet).accountId(account.getId()).build()).toWallet());
        }
        return this.repository.save(DynamodbAccount.from(account)
                .registrationDate(System.currentTimeMillis()).build()).toAccount();
    }

    @Override
    public Account getById(final Long id) {
        final DynamodbAccount account = this.repository.findOne(id);
        if (account != null) {
            return account.toAccount();
        }
        return null;
    }

    @Override
    public Account getByEmail(final String email) {
        final DynamodbAccount account = this.repository.findByEmail(email);
        if (account != null) {
            return account.toAccount();
        }
        return null;
    }

    @Override
    public Account update(final Long id, final Account account) throws ObjectNotFoundException {
        if (!this.exists(id)) {
            throw new ObjectNotFoundException(id, Account.class);
        }
        return this.repository.save(DynamodbAccount.from(account)
                .id(id).modificationDate(System.currentTimeMillis()).build()).toAccount();
    }

    @Override
    public Account delete(final Long id) throws ObjectNotFoundException {
        if (!this.exists(id)) {
            throw new ObjectNotFoundException(id, Account.class);
        }
        final DynamodbAccount account = this.repository.findOne(id);
        return this.repository.save(account
                .withStatus(Status.DEACTIVATED)
                .withModificationDate(System.currentTimeMillis())).toAccount();
    }

    @Override
    public List<Account> getAccounts() {
        return Lists.newArrayList(this.repository.findAll())
                .stream()
                .map(it -> it.withWallets(Lists.newArrayList()).toAccount())
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> getByType(final Type type) {
        return this.repository.findByType(type)
                .stream()
                .map(it -> it.withWallets(Lists.newArrayList()).toAccount())
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(final Long accountId) {
        return this.repository.exists(accountId);
    }

}
