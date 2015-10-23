/**
 *
 */
package io.yope.payment.rest.helpers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import groovy.util.logging.Slf4j;
import io.yope.payment.domain.Wallet;
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

    public Wallet update(final Long walletId, final Wallet wallet) throws ObjectNotFoundException {
        return walletService.update(walletId, wallet);
    }

    public List<Wallet> get(final Long id) {
        return walletService.get(id);
    }

    public Wallet getById(final Long walletId) {
        return walletService.getById(walletId);
    }

    public Wallet delete(final Long walletId) throws ObjectNotFoundException {
        return walletService.delete(walletId);
    }



}