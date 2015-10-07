/**
 *
 */
package io.yope.payment.rest.helpers;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.AccountTO;
import io.yope.payment.domain.transferobjects.WalletTO;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.rest.requests.RegistrationRequest;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.SecurityService;

/**
 * @author massi
 *
 */
@Service
public class AccountHelper {

    @Autowired
    private AccountService accountService;

    @Autowired
    private SecurityService securityService;

    public AccountTO registerAccount(final RegistrationRequest registration) {
        final Account account = AccountTO.builder()
                .email(registration.getEmail())
                .firstName(registration.getFirstName())
                .lastName(registration.getLastName())
                .type(registration.getType())
                .wallets(Sets.newLinkedHashSet())
                .build();
        String walletName = registration.getName();
        if (StringUtils.isEmpty(walletName)) {
            walletName = registration.getFirstName()+"'s Internal Wallet";
        }
        final Wallet inWallet =
                WalletTO.builder().
                        name(walletName).
                        hash(UUID.randomUUID().toString()).
                        description(registration.getDescription()).
                        type(Wallet.Type.INTERNAL).
                        build();
        Wallet exWallet = null;
        if (StringUtils.isNotBlank(registration.getHash())) {
            walletName = registration.getFirstName()+"'s External Wallet";
            exWallet = WalletTO.builder().
                    name(walletName).
                    hash(registration.getHash()).
                    type(Wallet.Type.EXTERNAL).
                    build();
        }
        final Account savedAccount = this.accountService.create(account, inWallet, exWallet);
        this.securityService.createCredentials(registration.getEmail(), registration.getPassword());
        return this.toAccounTO(savedAccount);

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
                .build();
    }

    public AccountTO update(final Long accountId, final AccountTO account) throws ObjectNotFoundException {
        return this.toAccounTO(this.accountService.update(accountId, account));
    }

    public AccountTO getById(final Long accountId) {
        return this.toAccounTO(this.accountService.getById(accountId));
    }

    public List<AccountTO> getAccounts() {
        final List<AccountTO> accountTOs = Lists.newArrayList();
        final List<Account> accounts = this.accountService.getAccounts();
        for (final Account account : accounts) {
            accountTOs.add(this.toAccounTO(account));
        }
        return accountTOs;
    }

    public AccountTO delete(final Long accountId) throws ObjectNotFoundException {
        return this.toAccounTO(this.accountService.delete(accountId));
    }

    public AccountTO getByEmail(final String email) {
        return this.toAccounTO(this.accountService.getByEmail(email));
    }



}
