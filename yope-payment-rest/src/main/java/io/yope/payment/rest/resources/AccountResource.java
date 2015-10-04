package io.yope.payment.rest.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.yope.payment.domain.Account;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.AccountTO;
import io.yope.payment.domain.transferobjects.WalletTO;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.rest.requests.RegistrationRequest;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * Account resource.
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/accounts")
@Slf4j
public class AccountResource {

    @Autowired
    private AccountService accountService;

    @Autowired
    private WalletService walletService;

    /**
     * Create Account.
     * @param response
     * @param registrationRequest
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Account> createAccount(
            final HttpServletResponse response,
            @RequestBody(required=true) final RegistrationRequest registrationRequest) {
        final Account account = AccountTO.builder()
                .email(registrationRequest.getEmail())
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .wallets(Sets.newLinkedHashSet())
                .build();
        String walletName = registrationRequest.getName();
        if (StringUtils.isNotEmpty(walletName)) {
            walletName = registrationRequest.getFirstName()+"'s Internal Wallet";
        }
        final Wallet inWallet =
                WalletTO.builder().
                        name(walletName).
                        hash(UUID.randomUUID().toString()).
                        description(registrationRequest.getDescription()).
                        type(Wallet.Type.INTERNAL).
                        build();
        Wallet savedInWallet = walletService.create(inWallet);
        account.getWallets().add(savedInWallet);
        if (StringUtils.isNotBlank(registrationRequest.getHash())) {
            walletName = registrationRequest.getFirstName()+"'s External Wallet";
            final Wallet exWallet = WalletTO.builder().
                    name(walletName).
                    hash(registrationRequest.getHash()).
                    type(Wallet.Type.EXTERNAL).
                    build();
            Wallet savedExWallet = walletService.create(exWallet);
            account.getWallets().add(savedExWallet);
        }
        Account savedAccount = accountService.create(account);
        AccountTO to = toAccounTO(savedAccount);
        final ResponseHeader header = new ResponseHeader(true, "");
        response.setStatus(Response.Status.CREATED.getStatusCode());
        return new PaymentResponse<Account>(header, to);
    }

    /**
     * Update Account.
      * @param accountId
     * @param account
     * @return
     */
    @RequestMapping(value="/{accountId}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Account> updateAccount(final HttpServletResponse response,
                                                                @PathVariable final long accountId,
                                                                @RequestBody(required=false) final AccountTO account) {
        final ResponseHeader header = new ResponseHeader(true, "");
        try {
            Account updated = accountService.update(accountId, account);
            return new PaymentResponse(header, toAccounTO(updated));
        } catch (ObjectNotFoundException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return new PaymentResponse(header.success(false).errorCode(e.getMessage()), account);
        }
    }

    /**
     * Get Account By ID.
     * @param accountId
     * @return
     */
    @RequestMapping(value="/{accountId}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PaymentResponse<Account> getAccount(@PathVariable final long accountId) {
        Account account = accountService.getById(accountId);
        final ResponseHeader header = new ResponseHeader(true, "");
        return new PaymentResponse<Account>(header, toAccounTO(account));
    }

    /**
     * List of existing accounts.
     * @param accountId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody PaymentResponse<List<AccountTO>> getAccounts(@PathVariable final long accountId) {
        List<AccountTO> accountTOs = Lists.newArrayList();
        List<Account> accounts = accountService.getAccounts();
        for (Account account : accounts) {
            accountTOs.add(toAccounTO(account));
        }
        final ResponseHeader header = new ResponseHeader(true, "");
        return new PaymentResponse<List<AccountTO>>(header, accountTOs);
    }

    /**
     * Delete Account.
     * @param accountId
     * @return
     */
    @RequestMapping(value="/{accountId}", method = RequestMethod.DELETE, produces = "application/json")
    public @ResponseBody PaymentResponse<Account> deleteAccount(final HttpServletResponse response,
                                                                @PathVariable final long accountId) {
        Account account = null;
        final ResponseHeader header = new ResponseHeader(true, "");
        try {
            account = accountService.delete(accountId);
            response.setStatus(Response.Status.CREATED.getStatusCode());
            return new PaymentResponse<Account>(header, toAccounTO(account));
        } catch (ObjectNotFoundException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return new PaymentResponse(header.success(false).errorCode(e.getMessage()), account);
        }
    }

    private AccountTO toAccounTO(Account savedAccount) {
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


}
