/**
 *
 */
package io.yope.payment.rest.helpers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import groovy.util.logging.Slf4j;
import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.blockchain.BlockchainException;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.QRImage;
import io.yope.payment.domain.transferobjects.WalletTO;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.services.WalletService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;

/**
 * @author massi
 *
 */
@Slf4j
@Service
public class WalletHelper {

    private static final int QR_WIDTH = 300;

    private static final int QR_HEIGHT = 300;

    @Autowired
    private WalletService walletService;

    @Autowired
    private BlockChainService blockChainService;

    public QRImage getQRImage(final Long accountId, final String name, final String reference, final String description, final BigDecimal amount) throws ObjectNotFoundException, BlockchainException {
        Wallet wallet = walletService.getByName(accountId, name);
        if (StringUtils.isBlank(wallet.getHash())) {
            final String hash = getHashForWallet(wallet.getContent());
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

    Image generateImage(final String hash, final String name, final String reference, final String description, final BigDecimal amount) {
        String code = generateCode(hash, reference, description, amount);
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new QRCodeWriter().encode(code, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            return bufferedImage.getScaledInstance(QR_WIDTH, QR_HEIGHT, Image.SCALE_DEFAULT);
        } catch (WriterException e) {
            //
        }
        return null;
    }

    private String generateCode(String hash, String reference, String description, BigDecimal amount) {
        return "bitcoin:" + hash + "?amount=" + amount.floatValue() + "&label=" + reference + "&message=" + description ;
    }

    private String getHashForWallet(byte[] content) throws BlockchainException {
        return blockChainService.generateHash(content);
    }


}
