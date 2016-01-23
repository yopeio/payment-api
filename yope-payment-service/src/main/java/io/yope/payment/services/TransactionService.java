/**
 *
 */
package io.yope.payment.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;
import com.google.common.hash.PrimitiveSink;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.blockchain.BlockchainException;
import io.yope.payment.blockchain.BlockchainSettings;
import io.yope.payment.db.services.TransactionDbService;
import io.yope.payment.db.services.WalletDbService;
import io.yope.payment.domain.QRImage;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Direction;
import io.yope.payment.domain.Transaction.Status;
import io.yope.payment.domain.Transaction.Type;
import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.BadRequestException;
import io.yope.payment.exceptions.IllegalTransactionStateException;
import io.yope.payment.exceptions.InsufficientFundsException;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.qr.QRHelper;
import io.yope.payment.transaction.services.TransactionStateService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author massi
 *
 */
@Slf4j
@Service
public class TransactionService {

    private static final int SCALE = 5;

    @Autowired
    private AccountService accountHelper;

    @Autowired
    private WalletService walletHelper;

    @Autowired
    private TransactionDbService transactionService;

    @Autowired
    private TransactionStateService transactionStateService;

    @Autowired
    private WalletDbService walletService;

    @Autowired
    private BlockChainService blockChainService;

    @Autowired
    private BlockchainSettings blockchainSettings;

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
        throw new BadRequestException("Transaction type not recognized "+transaction.getType()).field("type");
    }

    private Type getType(final Transaction transaction, final Long accountId) throws BadRequestException {
        final Wallet source = getWallet(transaction.getSource(), accountId);
        final Wallet destination = getWallet(transaction.getDestination(), accountId);
        if (source != null && Wallet.Type.INTERNAL.equals(source.getType()) &&
            destination != null && Wallet.Type.INTERNAL.equals(destination.getType())) {
            return Transaction.Type.TRANSFER;
        }
        if ((source == null || source != null && Wallet.Type.EXTERNAL.equals(source.getType())) &&
            destination != null && Wallet.Type.INTERNAL.equals(destination.getType())) {
            return Transaction.Type.DEPOSIT;
        }
        if (source != null && Wallet.Type.INTERNAL.equals(source.getType()) &&
            (destination == null || destination != null && Wallet.Type.EXTERNAL.equals(destination.getType()))) {
            return Transaction.Type.WITHDRAW;
        }
        throw new BadRequestException("Transaction not allowed");
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
        final BigDecimal correctedAmount = transaction.getAmount().setScale(SCALE, RoundingMode.FLOOR);
        final Wallet source = walletHelper.getByName(accountId, transaction.getSource().getName());
        if (source == null) {
            throw new ObjectNotFoundException(transaction.getSource().getId(), Wallet.class);
        }
        if (!canPay(source, correctedAmount)) {
            throw new InsufficientFundsException("not enough funds in the wallet with name "+source.getName());
        }
        final Wallet destination = walletHelper.getByName(accountId, transaction.getDestination().getName());
        if (destination == null) {
            throw new ObjectNotFoundException(transaction.getDestination().getId(), Wallet.class);
        }
        walletService.save(source.getId(), source.toBuilder()
                .balance(source.getBalance().subtract(correctedAmount))
                .availableBalance(source.getAvailableBalance().subtract(correctedAmount))
                .build());
        walletService.save(destination.getId(), destination.toBuilder()
                .balance(destination.getBalance().add(correctedAmount))
                .availableBalance(destination.getAvailableBalance().add(correctedAmount))
                .build());
        final Long now = System.currentTimeMillis();
        final Transaction.Builder pendingTransactionBuilder = transaction.toBuilder()
                .creationDate(now)
                .acceptedDate(now)
                .completedDate(now)
                .amount(correctedAmount)
                .balance(correctedAmount).blockchainFees(BigDecimal.ZERO).fees(BigDecimal.ZERO)
                .source(source).destination(destination).status(Status.COMPLETED);
        pendingTransactionBuilder.transactionHash(getInternalTransactionHash(pendingTransactionBuilder.build()));
        return transactionService.create(pendingTransactionBuilder.build());
    }

    private String getInternalTransactionHash(final Transaction transaction) {
        return Hashing.sha1().hashObject(transaction, new Funnel<Transaction>() {

            private static final long serialVersionUID = 9193015056720554840L;

            @Override
            public void funnel(final Transaction from, final PrimitiveSink into) {
                into.putUnencodedChars(from.getReference())
                    .putUnencodedChars(from.getSource().getName())
                    .putUnencodedChars(from.getDestination().getName())
                    .putFloat(from.getAmount().floatValue())
                    .putLong(from.getCreationDate());

            }
        }).toString();
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
            throw new ObjectNotFoundException(transaction.getDestination().getId(), Wallet.class);
        }
        final Transaction.Builder pendingTransactionBuilder = transaction.toBuilder().fees(BigDecimal.ZERO).source(source).destination(destination).status(Status.PENDING);
        final BigDecimal correctedAmount = transaction.getAmount().setScale(SCALE, RoundingMode.FLOOR);
        BigDecimal amountWithFee = correctedAmount;
        if (correctedAmount.floatValue() > blockchainSettings.getFeesThreshold().floatValue()) {
            amountWithFee = correctedAmount.add(blockchainSettings.getFees());
        }
        final QRImage qr = qrHelper.getQRImage(amountWithFee, blockChainService.generateCentralWalletHash());
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
            throw new ObjectNotFoundException(transaction.getSource().getId(), Wallet.class);
        }
        if (!canPay(source, correctedAmount)) {
            throw new InsufficientFundsException(MessageFormat.format("Insufficient Funds Exception in Wallet {0}", transaction.getSource()));
        }
        final Wallet destination = getWalletForWithdraw(transaction, accountId);
        final Transaction.Builder withdrawBuilder = transaction.toBuilder()
                .amount(correctedAmount)
                .fees(BigDecimal.ZERO)
                .source(source)
                .destination(destination).status(Status.PENDING);
        final Transaction withdraw = transactionService.create(withdrawBuilder.build());
        try {
            final String transactionHash = blockChainService.send(withdraw);
            transactionStateService.save(withdraw.getId(), withdrawBuilder.transactionHash(transactionHash).status(Status.ACCEPTED).build());
        } catch (final BlockchainException e) {
            log.error("Transaction "+withdraw.getId(), e);
            transactionService.save(withdraw.getId(), withdraw.toBuilder()
                    .failedDate(System.currentTimeMillis())
                    .status(Status.FAILED)
                    .transactionHash(getInternalTransactionHash(withdraw)).build());
            throw e;
        }
        return withdraw;
    }


    /**
     * TODO replace with check on PENDING/ACCEPTED transactions on this walled
     * suggested algorythm:
     * return SUM(incomingAcceptedTransaction.amount) - SUM(outgoingAcceptedTransaction.amount) > amount
     *
     * @param wallet
     * @param amount
     * @return
     */
    private boolean canPay(final Wallet wallet, final BigDecimal amount) {
        return wallet.getAvailableBalance().compareTo(amount) > 0;
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

    public Transaction save(final Long transactionId, final Status status) throws ObjectNotFoundException, InsufficientFundsException, IllegalTransactionStateException {
        final Transaction transaction = transactionService.get(transactionId);
        return transactionStateService.save(transactionId, transaction.toBuilder().status(status).build());
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