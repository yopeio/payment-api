/**
 *
 */
package io.yope.payment.rest.helpers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Account.Status;
import io.yope.payment.domain.Account.Type;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.AccountTO;
import io.yope.payment.domain.transferobjects.WalletTO;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.rest.requests.RegistrationRequest;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.SecurityService;
import io.yope.payment.services.WalletService;

/**
 * @author massi
 *
 */
@Service
public class AccountHelper {

    @Autowired
    private AccountService accountService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private SecurityService securityService;

    public AccountTO registerAccount(final RegistrationRequest registration) {
        Type type = registration.getType();
        if (type==null) {
            type = Type.SELLER;
        }
        final Account account = AccountTO.builder()
                .email(registration.getEmail())
                .firstName(registration.getFirstName())
                .lastName(registration.getLastName())
                .type(type)
                .status(Status.ACTIVE)
                .wallets(Sets.newLinkedHashSet())
                .build();
        String walletName = registration.getName();
        if (StringUtils.isEmpty(walletName)) {
            walletName = registration.getFirstName()+"'s Internal Wallet";
        }
        final Wallet inWallet =
                WalletTO.builder()
                .name(walletName)
                        .hash(UUID.randomUUID().toString())
                        .description(registration.getDescription())
                        .type(Wallet.Type.INTERNAL)
                        .balance(BigDecimal.ZERO)
                        .status(Wallet.Status.ACTIVE)
                        .build();
        Wallet exWallet = null;
        if (StringUtils.isNotBlank(registration.getHash())) {
            walletName = registration.getFirstName()+"'s External Wallet";
            exWallet = WalletTO.builder()
                    .name(walletName)
                    .hash(registration.getHash())
                    .type(Wallet.Type.EXTERNAL)
                    .status(Wallet.Status.ACTIVE)
                    .balance(BigDecimal.ZERO)
                    .build();
        }
        final Account savedAccount = accountService.create(account, inWallet, exWallet);
        securityService.createCredentials(registration.getEmail(), registration.getPassword(), registration.getType().toString());
        return toAccounTO(savedAccount);

    }

    public Wallet createWallet(final Account account, final Wallet wallet) throws ObjectNotFoundException {
        final WalletTO toSave =  WalletTO.builder().
                name(wallet.getName()).
                status(Wallet.Status.PENDING).
                type(wallet.getType()).
                balance(wallet.getBalance()).
                description(wallet.getDescription())
                .hash(UUID.randomUUID().toString())
                .build();
        final Wallet saved = walletService.create(toSave);
        account.getWallets().add(saved);
        accountService.update(account.getId(), account);
        return saved;
    }

    private AccountTO toAccounTO(final Account account) {
        return AccountTO.builder()
                .email(account.getEmail())
                .firstName(account.getFirstName())
                .id(account.getId())
                .lastName(account.getLastName())
                .modificationDate(account.getModificationDate())
                .registrationDate(account.getRegistrationDate())
                .wallets(account.getWallets())
                .type(account.getType())
                .status(account.getStatus())
                .build();
    }

    public AccountTO update(final Long accountId, final AccountTO account) throws ObjectNotFoundException {
        return toAccounTO(accountService.update(accountId, account));
    }

    public AccountTO getById(final Long accountId) {
        return toAccounTO(accountService.getById(accountId));
    }

    public List<AccountTO> getAccounts() {
        final List<AccountTO> accountTOs = Lists.newArrayList();
        final List<Account> accounts = accountService.getAccounts();
        for (final Account account : accounts) {
            accountTOs.add(toAccounTO(account));
        }
        return accountTOs;
    }

    public AccountTO delete(final Long accountId) throws ObjectNotFoundException {
        return toAccounTO(accountService.delete(accountId));
    }

    public AccountTO getByEmail(final String email) {
        return toAccounTO(accountService.getByEmail(email));
    }

    public boolean owns(final Account account, final Long walletId) {
        return account.getWallets().stream()
                .filter(o -> o.getId().equals(walletId))
                .findFirst().isPresent();
    }



}
