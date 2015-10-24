/**
 *
 */
package io.yope.payment.rest.helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.blockchain.BlockchainException;
import io.yope.payment.domain.QRImage;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Direction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.domain.Transaction.Type;
import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.IllegalTransactionStateException;
import io.yope.payment.exceptions.InsufficientFundsException;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.rest.BadRequestException;
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

    static final BigDecimal BLOCKCHAIN_FEES = new BigDecimal("0.1");

    private static final int SCALE = 5;

    @Autowired
    private AccountHelper accountHelper;

    @Autowired
    private WalletHelper walletHelper;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private BlockChainService blockChainService;

    @Autowired
    private QRHelper qrHelper;

    public Transaction create(final Transaction transaction, final Long accountId) throws ObjectNotFoundException, BadRequestException, BlockchainException, InsufficientFundsException, IllegalTransactionStateException {
        final Transaction.Type type = getType(transaction, accountId);
        switch (type) {
            case DEPOSIT:
                return doDeposit(transaction.toBuilder().type(Type.DEPOSIT).build(), accountId);
            case TRANSFER:
                return doTransfer(transaction.toBuilder().type(Type.TRANSFER).build(), accountId);
            case WITHDRAW:
                return doWithdraw(transaction.toBuilder().type(Type.WITHDRAW).build(), accountId);
            default:
                break;
        }
        throw new BadRequestException("Transaction type not recognized "+transaction.getType(), "type");
    }

    private Type getType(final Transaction transaction, final Long accountId) throws BadRequestException {
        final Wallet source = getWallet(transaction.getSource(), accountId);
        final Wallet destination = getWallet(transaction.getDestination(), accountId);
        if ((source != null && Wallet.Type.INTERNAL.equals(source.getType())) &&
            (destination != null && Wallet.Type.INTERNAL.equals(destination.getType()))) {
            return Transaction.Type.TRANSFER;
        }
        if ((source == null || (source != null && Wallet.Type.EXTERNAL.equals(source.getType()))) &&
            (destination != null && Wallet.Type.INTERNAL.equals(destination.getType()))) {
            return Transaction.Type.DEPOSIT;
        }
        if ((source != null && Wallet.Type.INTERNAL.equals(source.getType())) &&
            (destination == null || (destination != null && Wallet.Type.EXTERNAL.equals(destination.getType())))) {
            return Transaction.Type.WITHDRAW;
        }
        throw new BadRequestException("Transaction not allowed", null);
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
    public Transaction doTransfer(final Transaction transaction, final Long accountId) throws ObjectNotFoundException, BadRequestException, InsufficientFundsException {
        final Wallet source = walletHelper.getByName(accountId, transaction.getSource().getName());
        if (source == null) {
            throw new ObjectNotFoundException(MessageFormat.format("Source Wallet {0} Not Found", transaction.getSource()));
        }
        if (source.getAvailableBalance().compareTo(transaction.getAmount()) < 0) {
            throw new InsufficientFundsException("not enough funds in the wallet with name "+source.getName());
        }
        final Wallet destination = walletHelper.getByName(accountId, transaction.getDestination().getName());
        if (destination == null) {
            throw new ObjectNotFoundException(MessageFormat.format("Destination Wallet {0} Not Found", transaction.getDestination()));
        }
        final BigDecimal correctedAmount = transaction.getAmount().setScale(SCALE, RoundingMode.FLOOR);
        walletService.update(source.getId(), source.toBuilder()
                .balance(source.getBalance().subtract(correctedAmount))
                .availableBalance(source.getAvailableBalance().subtract(correctedAmount))
                .build());
        walletService.update(destination.getId(), destination.toBuilder()
                .balance(destination.getBalance().add(correctedAmount))
                .availableBalance(destination.getAvailableBalance().add(correctedAmount))
                .build());
        final Transaction.Builder pendingTransactionBuilder = transaction.toBuilder()
                .amount(correctedAmount)
                .balance(correctedAmount).blockchainFees(BigDecimal.ZERO).fees(BigDecimal.ZERO)
                .source(source).destination(destination).status(Status.COMPLETED);
        return transactionService.create(pendingTransactionBuilder.build());
    }

    /**
     * Transfers funds between an external wallet and an internal wallet belonging to the same seller.
     * @param transaction the transaction details
     * @param accountId the id of the seller
     * @return the pending transaction
     * @throws ObjectNotFoundException if the seller is not found
     * @throws BadRequestException if the internal wallet is not found
     */
    public Transaction doDeposit(final Transaction transaction, final Long accountId) throws ObjectNotFoundException, BadRequestException, BlockchainException {
        final Wallet source = getWalletForDeposit(transaction, accountId);
        final Wallet destination = walletHelper.getByName(accountId, transaction.getDestination().getName());
        if (destination == null) {
            throw new ObjectNotFoundException(MessageFormat.format("Destination Wallet {0} Not Found", transaction.getDestination()));
        }
        final Transaction.Builder pendingTransactionBuilder = transaction.toBuilder().fees(BigDecimal.ZERO).source(source).destination(destination).status(Status.PENDING);
        final BigDecimal correctedAmount = transaction.getAmount().setScale(SCALE, RoundingMode.FLOOR);
        final QRImage qr = qrHelper.getQRImage(correctedAmount.add(BLOCKCHAIN_FEES));
        pendingTransactionBuilder.QR(qr.getImageUrl()).receiverHash(qr.getHash());
        return transactionService.create(pendingTransactionBuilder.amount(correctedAmount).build());
    }

    private Wallet getWalletForDeposit(final Transaction transaction, final Long accountId) throws ObjectNotFoundException, BadRequestException {
        Wallet wallet = getWallet(transaction.getSource(), accountId);
        if (wallet == null) {
            wallet = accountHelper.saveWallet(
                        transaction.getSource().toBuilder().type(Wallet.Type.TRANSIT)
                        .availableBalance(transaction.getAmount())
                        .balance(transaction.getAmount()).build());
        }
        return wallet;
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
    public Transaction doWithdraw(final Transaction transaction, final Long accountId) throws BlockchainException, ObjectNotFoundException, BadRequestException, InsufficientFundsException, IllegalTransactionStateException {
        final BigDecimal correctedAmount = transaction.getAmount().setScale(SCALE, RoundingMode.FLOOR);
        final Wallet source = walletHelper.getByName(accountId, transaction.getSource().getName());
        if (source == null) {
            throw new ObjectNotFoundException(MessageFormat.format("Source Wallet {0} Not Found", transaction.getSource()));
        }
        if (source.getAvailableBalance().compareTo(correctedAmount) < 0) {
            throw new InsufficientFundsException(MessageFormat.format("Insufficient Funds Exception in Wallet {0}", transaction.getSource()));
        }
        final Wallet destination = getWalletForWithdraw(transaction, accountId);
        final Transaction.Builder pendingTransactionBuilder = transaction.toBuilder()
                .amount(correctedAmount)
                .fees(BigDecimal.ZERO)
                .source(source)
                .destination(destination).status(Status.PENDING);
        final Transaction pendingTransaction = transactionService.create(pendingTransactionBuilder.build());
        try {
            final String transactionHash = blockChainService.send(pendingTransaction);
            transactionService.save(pendingTransaction.getId(), pendingTransactionBuilder.transactionHash(transactionHash).status(Status.ACCEPTED).build());
        } catch (final Exception e) {
            log.error("Transaction "+pendingTransaction.getId(), e);
            transactionService.transition(pendingTransaction.getId(), Transaction.Status.FAILED);
            throw e;
        }
        return pendingTransaction;
    }

    private Wallet getWalletForWithdraw(final Transaction transaction, final Long accountId) throws ObjectNotFoundException, BadRequestException {
        final Wallet destination = transaction.getDestination();
        if (StringUtils.isBlank(destination.getWalletHash())) {
            final Wallet wallet = walletHelper.getByName(accountId, transaction.getDestination().getName());
            if (wallet == null) {
                throw new IllegalArgumentException(MessageFormat.format("Destination Wallet {0} Not Found", transaction.getDestination()));
            }
            if (wallet.getWalletHash() == null) {
                throw new IllegalArgumentException(MessageFormat.format("Destination Wallet {0} Has no Hash", transaction.getDestination()));
            }
            return wallet;
        }
        Wallet wallet = walletHelper.getByWalletHash(destination.getWalletHash());
        if (wallet != null) {
            return wallet;
        }
        wallet = walletHelper.getByName(accountId, transaction.getDestination().getName());
        if (wallet != null) {
            return accountHelper.saveWallet(wallet.toBuilder().walletHash(destination.getWalletHash()).build());
        }
        return accountHelper.createWallet(accountId, destination);
    }

    private Wallet getWallet(final Wallet source, final Long sellerId) {
        if (StringUtils.isNotBlank(source.getWalletHash())) {
            return walletHelper.getByWalletHash(source.getWalletHash());
        }
        return walletHelper.getByName(sellerId, source.getName());
    }


    public List<Transaction> getTransactionsForWallet(final Long walletId, final String reference, final Direction direction, final Status status, final Type type) throws ObjectNotFoundException {
        return transactionService.getForWallet(walletId, reference, direction, status, type);
    }

    public List<Transaction> getTransactionsForAccount(final Long accountId, final String reference, final Direction direction, final Status status, final Type type) throws ObjectNotFoundException {
        return transactionService.getForAccount(accountId, reference, direction, status, type);
    }

    public Transaction getTransactionById(final Long transactionId) {
        return transactionService.get(transactionId);
    }

    public Transaction doTransition(final Long transactionId, final Status status) throws ObjectNotFoundException, InsufficientFundsException, IllegalTransactionStateException {
        return transactionService.transition(transactionId, status);
    }

    public Transaction getTransactionByHash(final String hash) {
        return transactionService.getByTransactionHash(hash);
    }

    public Transaction getTransactionBySenderHash(final String hash) {
        return transactionService.getBySenderHash(hash);
    }

    public Transaction getTransactionByReceiverHash(final String hash) {
        return transactionService.getByReceiverHash(hash);
    }
}