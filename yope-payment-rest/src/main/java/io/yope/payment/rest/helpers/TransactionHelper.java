/**
 *
 */
package io.yope.payment.rest.helpers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.blockchain.BlockchainException;
import io.yope.payment.blockchain.bitcoinj.Constants;
import io.yope.payment.domain.Account;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Direction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.domain.Transaction.Type;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.QRImage;
import io.yope.payment.domain.transferobjects.TransactionTO;
import io.yope.payment.domain.transferobjects.WalletTO;
import io.yope.payment.exceptions.IllegalTransactionStateException;
import io.yope.payment.exceptions.InsufficientFundsException;
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

    public Transaction create(final TransactionTO transaction, final Long accountId) throws ObjectNotFoundException, BadRequestException, BlockchainException, InsufficientFundsException, IllegalTransactionStateException {
        switch (transaction.getType()) {
            case DEPOSIT:
                return deposit(transaction, accountId);
            case TRANSFER:
                return transfer(transaction, accountId);
            case WITHDRAW:
                return withdraw(transaction, accountId);
            default:
                break;
        }
        throw new BadRequestException("Transaction type not recognized "+transaction.getType(), "type");
    }


    /**
     * Transfers funds between 2 internal wallets belonging to the same seller.
     * @param transaction the transaction details
     * @param accountId the id of the seller
     * @return the pending transaction
     * @throws ObjectNotFoundException if the seller is not found
     * @throws BadRequestException if the wallets are not found
     * @throws InsufficientFundsException
     */
    public Transaction transfer(final Transaction transaction, final Long accountId) throws ObjectNotFoundException, BadRequestException, InsufficientFundsException {
        final Account seller = accountHelper.getById(accountId);
        final WalletTO source = getWallet(seller, transaction.getSource(), false, null);
        final WalletTO destination = getWallet(seller, transaction.getDestination(), false, null);
        if (source.getAvailableBalance().compareTo(transaction.getAmount()) < 0) {
            throw new InsufficientFundsException("not enough funds in the wallet with name "+source.getName());
        }
        walletService.update(source.getId(), Neo4JWallet.from(source)
                .balance(source.getBalance().subtract(transaction.getAmount()))
                .availableBalance(source.getAvailableBalance().subtract(transaction.getAmount()))
                .build());
        walletService.update(destination.getId(), Neo4JWallet.from(destination)
                .balance(destination.getBalance().add(transaction.getAmount()))
                .availableBalance(destination.getAvailableBalance().add(transaction.getAmount()))
                .build());
        final TransactionTO.Builder pendingTransactionBuilder = TransactionTO.from(transaction)
                .balance(transaction.getAmount()).blockchainFees(BigDecimal.ZERO).fees(BigDecimal.ZERO)
                .source(source).destination(destination).status(Status.COMPLETED);
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
        final TransactionTO.Builder pendingTransactionBuilder = TransactionTO.from(transaction).fees(BigDecimal.ZERO).source(source).destination(destination).status(Status.PENDING);
        final QRImage qr = getQRImage(transaction.getAmount());
        pendingTransactionBuilder.QR(qr.getImageUrl()).receiverHash(qr.getHash());
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
     * @throws IllegalTransactionStateException
     * @throws InsufficientFundsException
     */
    public Transaction withdraw(final Transaction transaction, final Long accountId) throws BlockchainException, ObjectNotFoundException, BadRequestException, InsufficientFundsException, IllegalTransactionStateException {
        final Account seller = accountHelper.getById(accountId);
        final WalletTO source = getWallet(seller, transaction.getSource(), false, null);
        final WalletTO destination = getWallet(seller, transaction.getDestination(), true, BigDecimal.ZERO);
        final TransactionTO.Builder pendingTransactionBuilder = TransactionTO.from(transaction).fees(BigDecimal.ZERO).source(source).destination(destination).status(Status.PENDING);
        final Transaction pendingTransaction = transactionService.create(pendingTransactionBuilder.build());
        if (source.getAvailableBalance().compareTo(transaction.getAmount()) < 0) {
            throw new InsufficientFundsException("not enough funds in the wallet with name "+source.getName());
        }
        try {
            blockChainService.send(pendingTransaction);
        } catch (final Exception e) {
            log.error("Transaction "+pendingTransaction.getId(), e);
            transactionService.transition(pendingTransaction.getId(), Transaction.Status.FAILED);
            throw e;
        }
        walletService.update(source.getId(), Neo4JWallet.from(source)
                .availableBalance(source.getAvailableBalance().subtract(transaction.getAmount())).build());
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
            final String imageName = hash+".png";
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
        return "bitcoin:" + hash + "?amount=" + amount.floatValue()/Constants.MILLI_BITCOINS;
    }

    private String getHash() throws BlockchainException {
        return blockChainService.generateCentralWalletHash();
    }

    private WalletTO getWallet(final Account seller, final Wallet source, final Boolean save, final BigDecimal amount) throws ObjectNotFoundException, BadRequestException {
        final Wallet wallet = getWallet(source, seller.getId());
        if (wallet == null) {
            if (save) {
                return WalletTO.from(accountHelper.saveWallet(WalletTO.from(source).type(Wallet.Type.TRANSIT).availableBalance(BigDecimal.ZERO).balance(amount).build())).build();
            }
            throw new BadRequestException("Wallet with name "+source.getName()+" does not exists", null);
        }
        if (save) {
            return WalletTO.from(accountHelper.saveWallet(WalletTO.from(wallet)
                    .availableBalance(BigDecimal.ZERO)
                    .balance(wallet.getAvailableBalance().add(amount))
                    .build())).build();
        }
        return WalletTO.from(wallet).build();
    }

    private Wallet getWallet(final Wallet source, final Long sellerId) {
        if (StringUtils.isNotBlank(source.getWalletHash())) {
            return walletService.getByWalletHash(source.getWalletHash());
        }
        return walletService.getByName(sellerId, source.getName());
    }


    public List<TransactionTO> getTransactionsForWallet(final Long walletId, final String reference, final Direction direction, final Status status, final Type type) throws ObjectNotFoundException {
        return transactionService.getForWallet(walletId, reference, direction, status, type).stream().map(t -> TransactionTO.from(t).build()).collect(Collectors.toList());
    }

    public List<TransactionTO> getTransactionsForAccount(final Long accountId, final String reference, final Direction direction, final Status status, final Type type) throws ObjectNotFoundException {
        return transactionService.getForAccount(accountId, reference, direction, status, type).stream().map(t -> TransactionTO.from(t).build()).collect(Collectors.toList());
    }

    public Transaction get(final Long transactionId) {
        final Transaction transaction = transactionService.get(transactionId);
        if (transaction == null) {
            return null;
        }
        return TransactionTO.from(transaction).build();
    }

    public Transaction transition(final Long transactionId, final Status status) throws ObjectNotFoundException, InsufficientFundsException, IllegalTransactionStateException {
        return TransactionTO.from(transactionService.transition(transactionId, status)).build();
    }

    public Transaction getByTransactionHash(final String hash) {
        final Transaction transaction = transactionService.getByTransactionHash(hash);
        if (transaction == null) {
            return null;
        }
        return TransactionTO.from(transaction).build();
    }

    public Transaction getBySenderHash(final String hash) {
        final Transaction transaction = transactionService.getBySenderHash(hash);
        if (transaction == null) {
            return null;
        }
        return TransactionTO.from(transaction).build();
    }

    public Transaction getByReceiverHash(final String hash) {
        final Transaction transaction = transactionService.getByReceiverHash(hash);
        if (transaction == null) {
            return null;
        }
        return TransactionTO.from(transaction).build();
    }
}
