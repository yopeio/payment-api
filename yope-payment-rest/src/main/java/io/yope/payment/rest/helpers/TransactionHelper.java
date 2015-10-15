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
import org.springframework.stereotype.Service;

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
import io.yope.payment.neo4j.domain.Neo4JWallet;
import io.yope.payment.rest.BadRequestException;
import io.yope.payment.rest.ServerConfiguration;
import io.yope.payment.services.TransactionService;
import io.yope.payment.services.WalletService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author massi
 *
 */
@Slf4j
@Service
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

    public Transaction create(final TransactionTO transaction, final Long accountId) throws ObjectNotFoundException, BadRequestException, BlockchainException {
        switch (transaction.getType()) {
            case DEPOSIT:
                return deposit(transaction, accountId);
            case TRANSFER:
                return deposit(transaction, accountId);
            case WITHDRAW:
                return withdraw(transaction, accountId);
            default:
                break;
        }
        throw new BadRequestException("Transaction type not recognized "+transaction.getType());
    }


    /**
     * Transfers funds between 2 internal wallets belonging to the same seller.
     * @param transaction the transaction details
     * @param accountId the id of the seller
     * @return the pending transaction
     * @throws ObjectNotFoundException if the seller is not found
     * @throws BadRequestException if the wallets are not found or if the balance is low
     */
    public Transaction transfer(final Transaction transaction, final Long accountId) throws ObjectNotFoundException, BadRequestException {
        final Account seller = accountHelper.getById(accountId);
        final WalletTO source = getWallet(seller, transaction.getSource(), false, null);
        if (source.getAvailableBalance().compareTo(transaction.getAmount()) < 0) {
            throw new BadRequestException("not enough funds in the wallet with name "+source.getName());
        }
        walletService.update(source.getId(), Neo4JWallet.from(source).availableBalance(source.getAvailableBalance().subtract(transaction.getAmount())).build());
        final WalletTO destination = getWallet(seller, transaction.getDestination(), false, null);
        final TransactionTO.Builder pendingTransactionBuilder = TransactionTO.from(transaction).source(source).destination(destination);
        final Transaction pendingTransaction = transactionService.create(pendingTransactionBuilder.build());
        return pendingTransaction;
    }

    /**
     * Transfers funds between an external wallet and an internal wallet belonging to the same seller.
     * @param transaction the transaction details
     * @param accountId the id of the seller
     * @return the pending transaction
     * @throws ObjectNotFoundException if the seller is not found
     * @throws BadRequestException if the internal wallet is not found
     */
    public Transaction deposit(final Transaction transaction, final Long accountId) throws ObjectNotFoundException, BadRequestException, BlockchainException {
        final Account seller = accountHelper.getById(accountId);
        final WalletTO source = getWallet(seller, transaction.getSource(), true, transaction.getAmount());
        final WalletTO destination = getWallet(seller, transaction.getDestination(), false, null);
        final TransactionTO.Builder pendingTransactionBuilder = TransactionTO.from(transaction).source(source).destination(destination);
        final QRImage qr = getQRImage(transaction.getAmount());
        pendingTransactionBuilder.QR(qr).senderHash(qr.getHash());
        final Transaction pendingTransaction = transactionService.create(pendingTransactionBuilder.build());
        return pendingTransaction;
    }

    /**
     * Transfers funds between an internal wallet and an external wallet belonging to the same seller.
     * @param transaction the transaction details
     * @param accountId the id of the seller
     * @return the pending transaction
     * @throws ObjectNotFoundException if the seller is not found
     * @throws BadRequestException if the internal wallet is not found or if the balance is low
     */
    public Transaction withdraw(final Transaction transaction, final Long accountId) throws BlockchainException, ObjectNotFoundException, BadRequestException {
        final Account seller = accountHelper.getById(accountId);
        final WalletTO source = getWallet(seller, transaction.getSource(), false, null);
        if (source.getAvailableBalance().compareTo(transaction.getAmount()) < 0) {
            throw new BadRequestException("not enough funds in the wallet with name "+source.getName());
        }
        walletService.update(source.getId(), Neo4JWallet.from(source).availableBalance(source.getAvailableBalance().subtract(transaction.getAmount())).build());
        final WalletTO destination = getWallet(seller, transaction.getDestination(), true, BigDecimal.ZERO);
        final TransactionTO.Builder pendingTransactionBuilder = TransactionTO.from(transaction).source(source).destination(destination);
        final Transaction pendingTransaction = transactionService.create(pendingTransactionBuilder.build());
        blockChainService.send(pendingTransaction);
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

    private WalletTO getWallet(final Account seller, final Wallet source, final Boolean create, final BigDecimal amount) throws ObjectNotFoundException, BadRequestException {
        final Wallet wallet = walletService.getByName(seller.getId(), source.getName());
        if (wallet == null) {
            if (create) {
                return WalletTO.from(accountHelper.createWallet(seller, WalletTO.from(source).availableBalance(BigDecimal.ZERO).balance(amount).build())).build();
            }
            throw new BadRequestException("Wallet with name "+source.getName()+" does not exists");
        }
        return WalletTO.from(wallet).build();
    }

}
