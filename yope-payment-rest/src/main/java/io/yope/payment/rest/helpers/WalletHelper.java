/**
 *
 */
package io.yope.payment.rest.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import groovy.util.logging.Slf4j;
import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.services.WalletService;

/**
 * @author massi
 *
 */
@Slf4j
@Service
public class WalletHelper {

    @Autowired
    private WalletService walletService;

    public boolean exists(final Long walletId) {
        return walletService.exists(walletId);
    }

    public Wallet save(final Wallet wallet) {
        return walletService.save(wallet);
    }

    public Wallet update(final Long walletId, final Wallet wallet) throws ObjectNotFoundException {
        return walletService.update(walletId, wallet);
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