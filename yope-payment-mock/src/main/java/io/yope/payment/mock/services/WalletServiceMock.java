package io.yope.payment.mock.services;

import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.mock.domain.WalletMock;
import io.yope.payment.services.WalletService;

import java.util.List;

/**
 * Created by enrico.mariotti on 30/09/2015.
 */
public class WalletServiceMock implements WalletService {
    @Override
    public Wallet create(Wallet wallet) {
        return new WalletMock().
                name(wallet.getName())
                .status(wallet.getStatus())
                .type(wallet.getType())
                .balance(wallet.getBalance())
                .description(wallet.getDescription())
                .hash(wallet.getHash())
                .privateKey(wallet.getPrivateKey())
                .content(wallet.getContent());
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
