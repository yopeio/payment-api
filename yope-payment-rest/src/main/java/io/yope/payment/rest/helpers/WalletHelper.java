/**
 *
 */
package io.yope.payment.rest.helpers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import groovy.util.logging.Slf4j;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.WalletTO;
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

    public Wallet update(final Long walletId, final WalletTO wallet) throws ObjectNotFoundException {
        return WalletTO.from(walletService.update(walletId, wallet)).build();
    }

    public List<Wallet> get(final Long id) {
        return Lists.newArrayList(walletService.get(id).stream().map(w -> WalletTO.from(w).build()).collect(Collectors.toList()));
    }

    public WalletTO getById(final Long walletId) {
        final Wallet wallet = walletService.getById(walletId);
        if (wallet != null) {
            return WalletTO.from(wallet).build();
        }
        return null;
    }

    public WalletTO getByName(final Long accountId, final String name) {
        final Wallet wallet = walletService.getByName(accountId, name);
        if (wallet != null) {
            return WalletTO.from(wallet).build();
        }
        return null;
    }

    public WalletTO delete(final Long walletId) throws ObjectNotFoundException {
        if (exists(walletId)) {
            return WalletTO.from(walletService.delete(walletId)).build();
        }
        throw new IllegalArgumentException(String.valueOf(walletId));
    }

    public WalletTO getByWalletHash(final String walletHash) {
        final Wallet wallet = walletService.getByWalletHash(walletHash);
        if (wallet != null) {
            return WalletTO.from(wallet).build();
        }
        return null;
    }



}