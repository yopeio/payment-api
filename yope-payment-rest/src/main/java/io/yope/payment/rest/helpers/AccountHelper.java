/**
 *
 */
package io.yope.payment.rest.helpers;

import java.math.BigDecimal;
import java.util.List;

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
import io.yope.payment.rest.BadRequestException;
import io.yope.payment.rest.requests.RegistrationRequest;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.UserSecurityService;
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
    private UserSecurityService securityService;

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
        final Wallet inWallet = WalletTO.builder()
                .availableBalance(BigDecimal.ZERO)
                .balance(BigDecimal.ZERO)
                .type(Wallet.Type.INTERNAL)
                .description(walletName)
                .name(walletName)
                .status(Wallet.Status.PENDING)
                .build();
        Wallet exWallet = null;
        if (StringUtils.isNotBlank(registration.getHash())) {
            final String walletDescription = registration.getFirstName()+"'s External Wallet";
            exWallet = WalletTO.builder()
                    .availableBalance(BigDecimal.ZERO)
                    .balance(BigDecimal.ZERO)
                    .type(Wallet.Type.EXTERNAL)
                    .description(walletDescription)
                    .name(registration.getFirstName())
                    .status(Wallet.Status.PENDING)
                    .build();
        }
        final Account savedAccount = accountService.create(account, inWallet, exWallet);
        securityService.createUser(registration.getEmail(), registration.getPassword(), registration.getType().toString());
        return AccountTO.from(savedAccount).build();

    }

    public Wallet saveWallet(final Wallet wallet) throws ObjectNotFoundException, BadRequestException {
        return walletService.save(wallet);
    }

    public Wallet saveWallet(final Account account, final Wallet wallet) throws ObjectNotFoundException, BadRequestException {
        final Wallet saved = walletService.save(wallet);
        account.getWallets().add(saved);
        accountService.update(account.getId(), account);
        return saved;
    }

    public Wallet createWallet(final Account account, final Wallet wallet) throws ObjectNotFoundException, BadRequestException {
        if (walletService.getByName(account.getId(), wallet.getName()) != null) {
            throw new BadRequestException("Wallet with name "+wallet.getName()+" does exists");
        }
        final WalletTO newWallet = WalletTO.from(wallet)
                .availableBalance(BigDecimal.ZERO)
                .balance(BigDecimal.ZERO)
                .type(StringUtils.isBlank(wallet.getHash())? Wallet.Type.INTERNAL : Wallet.Type.EXTERNAL)
                .status(Wallet.Status.PENDING)
                .build();
        return saveWallet(account, newWallet);
    }


    public AccountTO update(final Long accountId, final AccountTO account) throws ObjectNotFoundException {
        return AccountTO.from(accountService.update(accountId, account)).build();
    }

    public AccountTO getById(final Long accountId) {
        return AccountTO.from(accountService.getById(accountId)).build();
    }

    public List<AccountTO> getAccounts() {
        final List<AccountTO> accountTOs = Lists.newArrayList();
        final List<Account> accounts = accountService.getAccounts();
        for (final Account account : accounts) {
            accountTOs.add(AccountTO.from(account).build());
        }
        return accountTOs;
    }

    public AccountTO delete(final Long accountId) throws ObjectNotFoundException {
        return AccountTO.from(accountService.delete(accountId)).build();
    }

    public AccountTO getByEmail(final String email) {
        return AccountTO.from(accountService.getByEmail(email)).build();
    }

    public boolean owns(final Account account, final Long walletId) {
        return account.getWallets().stream()
                .filter(o -> o.getId().equals(walletId))
                .findFirst().isPresent();
    }



}
