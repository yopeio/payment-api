package io.yope.payment.mock.services;

import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.services.WalletService;

import java.util.List;

/**
 * Created by enrico.mariotti on 30/09/2015.
 */
public class WalletServiceMock implements WalletService {
    @Override
    public Wallet create(Wallet wallet) {
        return null;
    }

    @Override
    public Wallet getById(Long id) {
        return null;
    }

    @Override
    public Wallet getByHash(String hash) {
        return null;
    }

    @Override
    public Wallet update(Long id, Wallet wallet) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public Wallet delete(Long id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public List<Wallet> get(Long accountId) {
        return null;
    }
}
