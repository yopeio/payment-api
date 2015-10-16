/**
 *
 */
package io.yope.payment.rest.helpers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return walletService.get(id).stream().map(w -> WalletTO.from(w).build()).collect(Collectors.toList());
    }

    public Wallet getById(final Long walletId) {
        return WalletTO.from(walletService.getById(walletId)).build();
    }

    public Wallet delete(final Long walletId) throws ObjectNotFoundException {
        return WalletTO.from(walletService.delete(walletId)).build();
    }



}