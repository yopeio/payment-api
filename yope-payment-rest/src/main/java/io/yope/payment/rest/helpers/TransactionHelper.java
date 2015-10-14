/**
 *
 */
package io.yope.payment.rest.helpers;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.UUID;

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
import io.yope.payment.services.TransactionService;
import io.yope.payment.services.WalletService;

/**
 * @author massi
 *
 */
public class TransactionHelper {

    private static final int QR_WIDTH = 300;

    private static final int QR_HEIGHT = 300;


    @Autowired
    private AccountHelper accountHelper;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private BlockChainService blockChainService;

    public Transaction register(final Transaction transaction, final Long accountId) throws BlockchainException, ObjectNotFoundException {
        final Account seller = this.accountHelper.getById(accountId);
        final WalletTO source = this.getWallet(seller, transaction.getSource());
        final WalletTO destination = this.getWallet(seller, transaction.getDestination());
        final TransactionTO.Builder pendingTransactionBuilder = TransactionTO.from(transaction).source(source).destination(destination);
        if (Wallet.Type.INTERNAL.equals(source.getType()) &&
            Wallet.Type.INTERNAL.equals(destination.getType())) {
            final QRImage qr = this.getQRImage(transaction.getAmount());
            pendingTransactionBuilder.QR(qr).hash(qr.getHash());
        }
        final Transaction pendingTransaction = this.transactionService.create(pendingTransactionBuilder.build());
        if (Transaction.Type.EXTERNAL.equals(pendingTransaction.getType())) {
            this.blockChainService.send(pendingTransaction);
        }
        return pendingTransaction;
    }

    public QRImage getQRImage(final BigDecimal amount) throws ObjectNotFoundException, BlockchainException {
        final String hash = this.getHash();
        final Image image = this.generateImage(hash, amount);
        final String url = this.getUrl(image);
        return QRImage.builder()
                .amount(amount)
                .hash(hash)
                .imageUrl(url)
                .build();
    }

    private String getUrl(final Image image) {
        return null;
    }

    Image generateImage(final String hash, final BigDecimal amount) {
        final String code = this.generateCode(hash, amount);
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new QRCodeWriter().encode(code, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);
            final BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            return bufferedImage.getScaledInstance(QR_WIDTH, QR_HEIGHT, Image.SCALE_DEFAULT);
        } catch (final WriterException e) {
            //
        }
        return null;
    }

    private String generateCode(final String hash, final BigDecimal amount) {
        return "bitcoin:" + hash + "?amount=" + amount.floatValue();
    }

    private String getHash() throws BlockchainException {
        return UUID.randomUUID().toString();//this.blockChainService.generateHash();
    }

    private WalletTO getWallet(final Account seller, final Wallet source) throws ObjectNotFoundException {
        final Wallet wallet = this.walletService.getByName(seller.getId(), source.getName());
        if (wallet == null) {
            return WalletTO.from(this.accountHelper.createWallet(seller, WalletTO.from(wallet).build())).build();
        }
        return WalletTO.from(wallet).build();
    }



}
