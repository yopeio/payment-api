/**
 *
 */
package io.yope.payment.rest.helpers;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
import io.yope.payment.exceptions.DuplicateEmailException;
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

    public AccountTO registerAccount(final RegistrationRequest registration) throws DuplicateEmailException {
        if (accountService.getByEmail(registration.getEmail()) != null) {
            throw new DuplicateEmailException(registration.getEmail());
        }


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
        Wallet[] wallets = new Wallet[0];
        if (!Type.ADMIN.equals(account.getType())) {
            wallets = createWallets(registration);
        }
        final Account savedAccount = accountService.create(account, wallets);
        securityService.createUser(registration.getEmail(), registration.getPassword(), registration.getType().toString());
        return AccountTO.from(savedAccount).build();

    }

    private Wallet[] createWallets(final RegistrationRequest registration) {
        final List<Wallet> wallets = Lists.newArrayList();
        String walletName = registration.getName();
        if (StringUtils.isEmpty(walletName)) {
            walletName = registration.getFirstName()+"'s Internal Wallet";
        }
        wallets.add(WalletTO.builder()
                .availableBalance(BigDecimal.ZERO)
                .balance(BigDecimal.ZERO)
                .type(Wallet.Type.INTERNAL)
                .description(walletName)
                .name(walletName)
                .build());
        if (StringUtils.isNotBlank(registration.getHash())) {
            final String walletDescription = registration.getFirstName()+"'s External Wallet";
            wallets.add(WalletTO.builder()
                    .availableBalance(BigDecimal.ZERO)
                    .balance(BigDecimal.ZERO)
                    .type(Wallet.Type.EXTERNAL)
                    .description(walletDescription)
                    .name(registration.getFirstName())
                    .build());
        }
        return wallets.toArray(new Wallet[] {});
    }

    public WalletTO saveWallet(final Wallet wallet) throws ObjectNotFoundException, BadRequestException {
        return WalletTO.from(walletService.save(wallet)).build();
    }

    public Wallet saveWallet(final Account account, final Wallet wallet) throws ObjectNotFoundException, BadRequestException {
        final Wallet saved = walletService.save(wallet);
        account.getWallets().add(saved);
        accountService.update(account.getId(), account);
        return WalletTO.from(saved).build();
    }

    public Wallet createWallet(final Account account, final Wallet wallet) throws ObjectNotFoundException, BadRequestException {
        if (walletService.getByName(account.getId(), wallet.getName()) != null) {
            throw new BadRequestException("You already have a wallet with name "+wallet.getName(), "name");
        }

        final WalletTO newWallet = WalletTO.from(wallet)
                .availableBalance(BigDecimal.ZERO)
                .balance(BigDecimal.ZERO)
                .type(StringUtils.isBlank(wallet.getWalletHash())? Wallet.Type.INTERNAL : Wallet.Type.EXTERNAL)
                .build();
        return this.saveWallet(account, newWallet);
    }


    public AccountTO update(final Long accountId, final AccountTO account) throws ObjectNotFoundException {
        return AccountTO.from(accountService.update(accountId, account)).build();
    }

    public AccountTO getById(final Long accountId) {
        final Account account  = accountService.getById(accountId);
        if (account == null) {
            return null;
        }
        return AccountTO.from(account).build();
    }

    public List<AccountTO> getAccounts() {
        return accountService.getAccounts().stream().map(a -> AccountTO.from(a).build()).collect(Collectors.toList());
    }

    public AccountTO delete(final Long accountId) throws ObjectNotFoundException {
        final Account account  = accountService.delete(accountId);
        if (account == null) {
            return null;
        }
        securityService.deleteUser(account.getEmail());
        return AccountTO.from(account).build();

    }

    public AccountTO getByEmail(final String email) {
        final Account account  = accountService.getByEmail(email);
        if (account == null) {
            return null;
        }
        return AccountTO.from(account).build();
    }

    public boolean owns(final Account account, final Long walletId) {
        return account.getWallets().stream().anyMatch(w -> w.getId().equals(walletId));
    }



}
