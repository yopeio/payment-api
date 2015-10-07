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

/**
 * @author massi
 *
 */
@Service
public class AccountHelper {

    @Autowired
    private AccountService accountService;

    public AccountTO registerAccount(final RegistrationRequest registrationRequest) {
        final Account account = AccountTO.builder()
                .email(registrationRequest.getEmail())
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .wallets(Sets.newLinkedHashSet())
                .build();
        String walletName = registrationRequest.getName();
        if (StringUtils.isEmpty(walletName)) {
            walletName = registrationRequest.getFirstName()+"'s Internal Wallet";
        }
        final Wallet inWallet =
                WalletTO.builder().
                        name(walletName).
                        hash(UUID.randomUUID().toString()).
                        description(registrationRequest.getDescription()).
                        type(Wallet.Type.INTERNAL).
                        build();
        Wallet exWallet = null;
        if (StringUtils.isNotBlank(registrationRequest.getHash())) {
            walletName = registrationRequest.getFirstName()+"'s External Wallet";
            exWallet = WalletTO.builder().
                    name(walletName).
                    hash(registrationRequest.getHash()).
                    type(Wallet.Type.EXTERNAL).
                    build();
        }
        final Account savedAccount = accountService.create(account, inWallet, exWallet);
        return toAccounTO(savedAccount);

    }

    private AccountTO toAccounTO(final Account savedAccount) {
        return AccountTO.builder()
                .email(savedAccount.getEmail())
                .firstName(savedAccount.getFirstName())
                .id(savedAccount.getId())
                .lastName(savedAccount.getLastName())
                .modificationDate(savedAccount.getModificationDate())
                .registrationDate(savedAccount.getRegistrationDate())
                .wallets(savedAccount.getWallets())
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



}
