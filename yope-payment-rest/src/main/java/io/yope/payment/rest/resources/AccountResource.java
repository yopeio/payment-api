package io.yope.payment.rest.resources;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.AccountTO;
import io.yope.payment.domain.transferobjects.WalletTO;
import io.yope.payment.rest.requests.RegistrationRequest;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.WalletService;
import lombok.extern.slf4j.Slf4j;

/**
 * Account resource.
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/transactions")
@Slf4j
public class AccountResource {

    @Autowired
    private AccountService accountService;

    @Autowired
    private WalletService walletService;

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody
    PaymentResponse<Account> createPlayer(
            final HttpSession session,
            final HttpServletRequest request,
            final HttpServletResponse response,
            @RequestBody(required=true) final RegistrationRequest registrationRequest) {
        final Account account = AccountTO.builder()
                .email(registrationRequest.getEmail())
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .build();
        String walletName = registrationRequest.getName();
        if (StringUtils.isNotEmpty(walletName)) {
            walletName = registrationRequest.getFirstName()+"'s Internal Wallet";
        }
        final Wallet inWallet = WalletTO.builder().name(walletName).description(registrationRequest.getDescription()).build();
        walletService.create(inWallet);
        account.getWallets().add(inWallet);
        if (StringUtils.isNotBlank(registrationRequest.getHash())) {
            walletName = registrationRequest.getFirstName()+"'s External Wallet";
            final Wallet exWallet = WalletTO.builder().name(walletName).hash(registrationRequest.getHash()).build();
            walletService.create(exWallet);
            account.getWallets().add(exWallet);
        }
        accountService.create(account);
        final ResponseHeader header = new ResponseHeader(true, "");
        return new PaymentResponse<Account>(header, account);
    }

//    @RequestMapping(method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
//
//    @RequestMapping(value="/{playerId}", method = RequestMethod.GET, produces = "application/json")





}
