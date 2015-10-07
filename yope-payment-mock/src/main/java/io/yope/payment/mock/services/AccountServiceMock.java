package io.yope.payment.mock.services;

import java.util.ArrayList;
import java.util.List;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.services.AccountService;

/**
 * Created by enrico.mariotti on 30/09/2015.
 */
public class AccountServiceMock implements AccountService {
    @Override
    public Account create(final Account account, final Wallet... wallets) {
        return null;
    }

    @Override
    public Account getById(final Long id) {
        return null;
    }

    @Override
    public Account update(final Long id, final Account account) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public Account delete(final Long id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public List<Account> getAccounts() {
        return new ArrayList<Account>();
    }
}
