/**
 *
 */
package io.yope.payment.rest.helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.blockchain.BlockchainException;
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

    public Transaction create(final TransactionTO transaction, final Long accountId) throws ObjectNotFoundException, BadRequestException, BlockchainException, InsufficientFundsException, IllegalTransactionStateException {
        final Transaction.Type type = getType(transaction, accountId);
        switch (type) {
            case DEPOSIT:
                return doDeposit(TransactionTO.from(transaction).type(Type.DEPOSIT).build(), accountId);
            case TRANSFER:
                return doTransfer(TransactionTO.from(transaction).type(Type.TRANSFER).build(), accountId);
            case WITHDRAW:
                return doWithdraw(TransactionTO.from(transaction).type(Type.WITHDRAW).build(), accountId);
            default:
                break;
        }
        throw new BadRequestException("Transaction type not recognized "+transaction.getType(), "type");
    }

    private Type getType(final TransactionTO transaction, final Long accountId) throws BadRequestException {
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
        final WalletTO source = walletHelper.getByName(accountId, transaction.getSource().getName());
        if (source == null) {
            throw new ObjectNotFoundException("Source Wallet Not Found", Wallet.class);
        }
        if (source.getAvailableBalance().compareTo(transaction.getAmount()) < 0) {
            throw new InsufficientFundsException("not enough funds in the wallet with name "+source.getName());
        }
        final WalletTO destination = walletHelper.getByName(accountId, transaction.getDestination().getName());
        if (destination == null) {
            throw new ObjectNotFoundException("Destination Wallet Not Found", Wallet.class);
        }
        final BigDecimal correctedAmount = transaction.getAmount().setScale(SCALE, RoundingMode.FLOOR);
        walletService.update(source.getId(), Neo4JWallet.from(source)
                .balance(source.getBalance().subtract(correctedAmount))
                .availableBalance(source.getAvailableBalance().subtract(correctedAmount))
                .build());
        walletService.update(destination.getId(), Neo4JWallet.from(destination)
                .balance(destination.getBalance().add(correctedAmount))
                .availableBalance(destination.getAvailableBalance().add(correctedAmount))
                .build());
        final TransactionTO.Builder pendingTransactionBuilder = TransactionTO.from(transaction)
                .amount(correctedAmount)
                .balance(correctedAmount).blockchainFees(BigDecimal.ZERO).fees(BigDecimal.ZERO)
                .source(source).destination(destination).status(Status.COMPLETED);
        final Transaction pendingTransaction = transactionService.create(pendingTransactionBuilder.build());
        return TransactionTO.from(pendingTransaction).build();
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
        final WalletTO source = getWalletForDeposit(transaction, accountId);
        final WalletTO destination = walletHelper.getByName(accountId, transaction.getDestination().getName());
        if (destination == null) {
            throw new ObjectNotFoundException("Destination Wallet Not Found", Wallet.class);
        }
        final TransactionTO.Builder pendingTransactionBuilder = TransactionTO.from(transaction).fees(BigDecimal.ZERO).source(source).destination(destination).status(Status.PENDING);
        final BigDecimal correctedAmount = transaction.getAmount().setScale(SCALE, RoundingMode.FLOOR);
        final QRImage qr = qrHelper.getQRImage(correctedAmount.add(BLOCKCHAIN_FEES));
        pendingTransactionBuilder.QR(qr.getImageUrl()).receiverHash(qr.getHash());
        final Transaction pendingTransaction = transactionService.create(pendingTransactionBuilder.amount(correctedAmount).build());
        return TransactionTO.from(pendingTransaction).build();
    }

    private WalletTO getWalletForDeposit(final Transaction transaction, final Long accountId) throws ObjectNotFoundException, BadRequestException {
        WalletTO wallet = getWallet(transaction.getSource(), accountId);
        if (wallet == null) {
            wallet = accountHelper.saveWallet(
                    WalletTO.from(transaction.getSource()).type(Wallet.Type.TRANSIT)
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
        final WalletTO source = walletHelper.getByName(accountId, transaction.getSource().getName());
        if (source == null) {
            throw new ObjectNotFoundException("Destination Wallet Not Found", Wallet.class);
        }
        if (source.getAvailableBalance().compareTo(correctedAmount) < 0) {
            throw new InsufficientFundsException("not enough funds in the wallet with name "+source.getName());
        }
        WalletTO destination = getWalletForWithdraw(transaction, accountId);
        if (destination == null) {
            if (transaction.getDestination().getWalletHash() == null) {
                throw new IllegalArgumentException("missing hash for destination");
            }
            destination = accountHelper.saveWallet(WalletTO.from(transaction.getDestination()).build());
        }
        if (destination.getWalletHash() == null) {
            throw new IllegalArgumentException("missing hash for destination");
        }
        final TransactionTO.Builder pendingTransactionBuilder = TransactionTO.from(transaction)
                .amount(correctedAmount)
                .fees(BigDecimal.ZERO)
                .source(source)
                .destination(destination).status(Status.PENDING);
        final Transaction pendingTransaction = transactionService.create(pendingTransactionBuilder.build());
        try {
            blockChainService.send(pendingTransaction);
        } catch (final Exception e) {
            log.error("Transaction "+pendingTransaction.getId(), e);
            transactionService.transition(pendingTransaction.getId(), Transaction.Status.FAILED);
            throw e;
        }
        return TransactionTO.from(pendingTransaction).build();
    }

    private WalletTO getWalletForWithdraw(final Transaction transaction, final Long accountId) throws ObjectNotFoundException, BadRequestException {
        final Wallet destination = transaction.getDestination();
        if (StringUtils.isBlank(destination.getWalletHash())) {
            final WalletTO wallet = walletHelper.getByName(accountId, transaction.getDestination().getName());
            if (wallet == null) {
                throw new IllegalArgumentException("missing destination wallet");
            }
            if (wallet.getWalletHash() == null) {
                throw new IllegalArgumentException("missing hash for destination");
            }
            return wallet;
        }
        final WalletTO wallet = walletHelper.getByWalletHash(destination.getWalletHash());
        if (wallet != null) {
            return wallet;
        }
        return accountHelper.saveWallet(WalletTO.from(destination).build());
    }

    private WalletTO getWallet(final Wallet source, final Long sellerId) {
        if (StringUtils.isNotBlank(source.getWalletHash())) {
            return walletHelper.getByWalletHash(source.getWalletHash());
        }
        return walletHelper.getByName(sellerId, source.getName());
    }


    public List<TransactionTO> getTransactionsForWallet(final Long walletId, final String reference, final Direction direction, final Status status, final Type type) throws ObjectNotFoundException {
        return transactionService.getForWallet(walletId, reference, direction, status, type).stream().map(t -> TransactionTO.from(t).build()).collect(Collectors.toList());
    }

    public List<TransactionTO> getTransactionsForAccount(final Long accountId, final String reference, final Direction direction, final Status status, final Type type) throws ObjectNotFoundException {
        return transactionService.getForAccount(accountId, reference, direction, status, type).stream().map(t -> TransactionTO.from(t).build()).collect(Collectors.toList());
    }

    public Transaction getTransactionById(final Long transactionId) {
        final Transaction transaction = transactionService.get(transactionId);
        if (transaction == null) {
            return null;
        }
        return TransactionTO.from(transaction).build();
    }

    public Transaction doTransition(final Long transactionId, final Status status) throws ObjectNotFoundException, InsufficientFundsException, IllegalTransactionStateException {
        return TransactionTO.from(transactionService.transition(transactionId, status)).build();
    }

    public Transaction getTransactionByHash(final String hash) {
        final Transaction transaction = transactionService.getByTransactionHash(hash);
        if (transaction == null) {
            return null;
        }
        return TransactionTO.from(transaction).build();
    }

    public Transaction getTransactionBySenderHash(final String hash) {
        final Transaction transaction = transactionService.getBySenderHash(hash);
        if (transaction == null) {
            return null;
        }
        return TransactionTO.from(transaction).build();
    }

    public Transaction getTransactionByReceiverHash(final String hash) {
        final Transaction transaction = transactionService.getByReceiverHash(hash);
        if (transaction == null) {
            return null;
        }
        return TransactionTO.from(transaction).build();
    }
}
