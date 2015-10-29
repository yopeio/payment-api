/**
 *
 */
package io.yope.payment.services;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.yope.payment.db.services.WalletDbService;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.Wallet.Status;
import io.yope.payment.exceptions.ObjectNotFoundException;

/**
 * @author massi
 *
 */
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
        final Wallet current = getById(walletId);
        if (current == null) {
            throw new ObjectNotFoundException(MessageFormat.format("Wallet with id {0} Not Found", walletId));
        }
        final Wallet toSave = wallet.toBuilder()
                .id(walletId)
                .name(StringUtils.isNotBlank(wallet.getName())? wallet.getName() : current.getName())
                .description(StringUtils.isNotBlank(wallet.getDescription())? wallet.getDescription() : current.getDescription())
                .privateKey(StringUtils.isNotBlank(wallet.getPrivateKey())? wallet.getPrivateKey() : current.getPrivateKey())
                .content(StringUtils.isNotBlank(wallet.getContent())? wallet.getContent() : current.getContent())
                .walletHash(StringUtils.isNotBlank(wallet.getWalletHash())? wallet.getWalletHash() : current.getWalletHash())
                .availableBalance(current.getAvailableBalance())
                .balance(current.getBalance())
                .creationDate(current.getCreationDate())
                .status(current.getStatus())
                .type(current.getType())
                .build();
        return walletService.update(walletId, toSave);
    }

    public List<Wallet> get(final Long id, final Status status) {
        return walletService.getWalletsByAccountId(id, status);
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