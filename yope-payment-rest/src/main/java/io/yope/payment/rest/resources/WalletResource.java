package io.yope.payment.rest.resources;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.blockchain.BlockchainException;
import io.yope.payment.domain.Wallet;
import io.yope.payment.mock.domain.WalletMock;
import io.yope.payment.services.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
    @Autowired
    private BlockChainService blockChainService;

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody PaymentResponse<Wallet> createWallet(
            @RequestBody(required=false) WalletMock wallet) {
        ResponseHeader header = new ResponseHeader(true, "");
        WalletMock result = null;
        try {
            Wallet registered = blockChainService.register();
            Wallet toSave = new WalletMock().
                    name(wallet.getName()).
                    status(wallet.getStatus()).
                    type(wallet.getType()).
                    balance(wallet.getBalance()).
                    description(wallet.getDescription()).
                    content(registered.getContent()).
                    hash(registered.getHash()).
                    privateKey(registered.getPrivateKey());
            result = (WalletMock)walletService.create(toSave);
            result.privateKey("").content(new byte[0]);
            return new PaymentResponse(header, result);
        } catch (BlockchainException e) {
            return new PaymentResponse(header.success(false).errorCode(e.getErrorCode()), result);
        }
    }
}
