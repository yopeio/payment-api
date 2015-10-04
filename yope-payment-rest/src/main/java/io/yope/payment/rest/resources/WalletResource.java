package io.yope.payment.rest.resources;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.WalletTO;
import io.yope.payment.services.WalletService;
import lombok.extern.slf4j.Slf4j;

/**
 * Wallet Resource.
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/wallets")
@Slf4j
public class WalletResource {

    @Autowired
    private WalletService walletService;


    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Wallet> createWallet(
            @RequestBody(required=false) final WalletTO wallet) {
        final ResponseHeader header = new ResponseHeader(true, "");
        Wallet saved = null;
        final WalletTO toSave =  WalletTO.builder().
                name(wallet.getName()).
                status(wallet.getStatus()).
                type(wallet.getType()).
                balance(wallet.getBalance()).
                description(wallet.getDescription())
                .hash(UUID.randomUUID().toString())
                .build();
        saved = walletService.create(toSave);

        return new PaymentResponse(header, saved);
    }

    @RequestMapping(method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public List<Wallet> getWallets() {
        return walletService.get(35L);
    }
}
