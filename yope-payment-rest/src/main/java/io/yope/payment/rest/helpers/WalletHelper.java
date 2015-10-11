/**
 *
 */
package io.yope.payment.rest.helpers;

import java.awt.Image;
import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.blockchain.BlockchainException;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.QRImage;
import io.yope.payment.domain.transferobjects.WalletTO;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.services.WalletService;

/**
 * @author massi
 *
 */
@Service
public class WalletHelper {

    @Autowired
    private WalletService walletService;

    private BlockChainService blockChainService;

    public QRImage getQRImage(final Long accountId, final String name, final String reference, final String description, final BigDecimal amount) throws ObjectNotFoundException, BlockchainException {
        Wallet wallet = walletService.getByName(accountId, name);
        if (StringUtils.isBlank(wallet.getHash())) {
            final String hash = getHashForWallet(wallet);
            wallet = walletService.update(wallet.getId(), WalletTO.from(wallet).hash(hash).modificationDate(System.currentTimeMillis()).status(Wallet.Status.ACTIVE).build());
        }
        final Image image = generateImage(wallet.getHash(), name, reference, description, amount);
        return QRImage.builder()
                .amount(amount)
                .hash(wallet.getHash())
                .name(name)
                .description(description)
                .image(image)
                .build();
    }

    private Image generateImage(final String hash, final String name, final String reference, final String description, final BigDecimal amount) {
        return null;
    }

    private String getHashForWallet(final Wallet wallet) throws BlockchainException {
        return blockChainService.generateHash(wallet);
    }


}
