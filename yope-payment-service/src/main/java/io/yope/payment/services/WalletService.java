/**
 *
 */
package io.yope.payment.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.yope.payment.db.services.WalletDbService;
import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.ObjectNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author massi
 *
 */
@Slf4j
@Service
public class WalletService {

    @Autowired
    private WalletDbService walletService;

    public boolean exists(final Long walletId) {
        return walletService.exists(walletId);
    }

    public Wallet save(final Wallet wallet) {
        return walletService.save(wallet);
    }

    public Wallet update(final Long walletId, final Wallet wallet) throws ObjectNotFoundException {
        return walletService.update(walletId, wallet);
    }

    public List<Wallet> get(final Long id) {
        return walletService.get(id);
    }

    public Wallet getById(final Long walletId) {
        return walletService.getById(walletId);
    }

    public Wallet getByName(final Long accountId, final String name) {
        return walletService.getByName(accountId, name);
    }

    public Wallet delete(final Long walletId) throws ObjectNotFoundException {
        if (exists(walletId)) {
            return walletService.delete(walletId);
        }
        throw new IllegalArgumentException(String.valueOf(walletId));
    }

    public Wallet getByWalletHash(final String walletHash) {
        return walletService.getByWalletHash(walletHash);
    }



}