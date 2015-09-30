package io.yope.payment.mock.services;

import io.yope.payment.domain.Account;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.services.AccountService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico.mariotti on 30/09/2015.
 */
public class AccountServiceMock implements AccountService {
    @Override
    public Account create(Account account) {
        return null;
    }

    @Override
    public Account getById(Long id) {
        return null;
    }

    @Override
    public Account update(Long id, Account account) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public Account delete(Long id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public List<Account> getAccounts() {
        return new ArrayList();
    }
}
