/**
 *
 */
package io.yope.payment.rest.helpers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.blockchain.BlockchainException;
import io.yope.payment.domain.Account;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.QRImage;
import io.yope.payment.domain.transferobjects.TransactionTO;
import io.yope.payment.domain.transferobjects.WalletTO;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.rest.ServerConfiguration;
import io.yope.payment.services.TransactionService;
import io.yope.payment.services.WalletService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author massi
 *
 */
@Slf4j
public class TransactionHelper {

    private static final int QR_WIDTH = 300;

    private static final int QR_HEIGHT = 300;

    @Autowired
    private ServerConfiguration serverConfiguration;

    @Autowired
    private AccountHelper accountHelper;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private BlockChainService blockChainService;



    public Transaction register(final Transaction transaction, final Long accountId) throws BlockchainException, ObjectNotFoundException {
        final Account seller = accountHelper.getById(accountId);
        final WalletTO source = getWallet(seller, transaction.getSource());
        final WalletTO destination = getWallet(seller, transaction.getDestination());
        final TransactionTO.Builder pendingTransactionBuilder = TransactionTO.from(transaction).source(source).destination(destination);
        if (Wallet.Type.INTERNAL.equals(source.getType()) &&
            Wallet.Type.INTERNAL.equals(destination.getType())) {
            final QRImage qr = getQRImage(transaction.getAmount());
            pendingTransactionBuilder.QR(qr).hash(qr.getHash());
        }
        final Transaction pendingTransaction = transactionService.create(pendingTransactionBuilder.build());
        if (Transaction.Type.EXTERNAL.equals(pendingTransaction.getType())) {
            blockChainService.send(pendingTransaction);
        }
        return pendingTransaction;
    }

    public QRImage getQRImage(final BigDecimal amount) throws ObjectNotFoundException, BlockchainException {
        final String hash = getHash();
        final String url = generateImageUrl(hash, amount);
        return QRImage.builder()
                .amount(amount)
                .hash(hash)
                .imageUrl(url)
                .build();
    }

    String generateImageUrl(final String hash, final BigDecimal amount) {
        final String code = generateCode(hash, amount);
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new QRCodeWriter().encode(code, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);
            final BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            final String imageName = UUID.randomUUID().toString()+".png";
            final File image = new File(getImageFolder(), imageName);
            ImageIO.write(bufferedImage, "png", image);
            return serverConfiguration.getImageAbsolutePath() + imageName;
        } catch (final WriterException e) {
            log.error("creating image", e);
        } catch (final IOException e) {
            log.error("saving image", e);
        }
        return null;
    }

    private File getImageFolder() {
        final File folder = new File(serverConfiguration.getImageFolder());
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    private String generateCode(final String hash, final BigDecimal amount) {
        return "bitcoin:" + hash + "?amount=" + amount.floatValue();
    }

    private String getHash() throws BlockchainException {
        return UUID.randomUUID().toString();//this.blockChainService.generateHash();
    }

    private WalletTO getWallet(final Account seller, final Wallet source) throws ObjectNotFoundException {
        final Wallet wallet = walletService.getByName(seller.getId(), source.getName());
        if (wallet == null) {
            return WalletTO.from(accountHelper.createWallet(seller, WalletTO.from(wallet).build())).build();
        }
        return WalletTO.from(wallet).build();
    }



}
