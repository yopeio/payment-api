package io.yope.payment.blockchain.bitcoinj;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import javax.xml.bind.DatatypeConverter;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.TransactionTO;
import io.yope.payment.domain.transferobjects.WalletTO;
import io.yope.payment.exceptions.IllegalTransactionStateException;
import io.yope.payment.exceptions.InsufficientFundsException;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.services.TransactionService;
import io.yope.payment.services.WalletService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class WalletEventListener extends AbstractWalletEventListener {

    private final PeerGroup peerGroup;
    private final NetworkParameters params;
    private final TransactionService transactionService;
    private final BlockchainSettings settings;
    private final WalletService walletService;
    private final Wallet centralWallet;

    @Override
    public void onTransactionConfidenceChanged(final org.bitcoinj.core.Wallet wallet,
                                               final org.bitcoinj.core.Transaction tx) {


        receiveCoins(wallet,tx);

        final String transactionHash = tx.getHashAsString();
        final Transaction loaded = transactionService.getByTransactionHash(transactionHash);
        if (loaded == null) {
            log.warn("transaction hash {} does not yet exist", transactionHash);
            return;
        }
        log.info("-----> confidence changed on {} {}", loaded.getId(), transactionHash);
        final TransactionConfidence confidence = tx.getConfidence();
        log.info("new block depth: {} ", confidence.getDepthInBlocks());
        if (confidence.getDepthInBlocks() >= settings.getConfirmations()) {
            if (!Transaction.Status.ACCEPTED.equals(loaded.getStatus())) {
                log.error("transaction {} has status {}", loaded.getId(), loaded.getStatus());
                return;
            }
            try {
                transactionService.transition(loaded.getId(), Transaction.Status.COMPLETED);
            } catch (final ObjectNotFoundException e) {
                log.error("transaction not found", e);
            } catch (final InsufficientFundsException e) {
                log.error("Insufficient money", e);
            } catch (final IllegalTransactionStateException e) {
                log.error("Illegal transaction state", e);
            }
        }
    }

    private void receiveCoins(final org.bitcoinj.core.Wallet wallet,
                                             final org.bitcoinj.core.Transaction tx) {
//        super.onCoinsReceived(wallet, tx, prevBalance, newBalance);
        final Coin valueSentToMe = tx.getValueSentToMe(wallet);
        final Coin valueSentFromMe = tx.getValueSentFromMe(wallet);

        final DeterministicKey freshKey = wallet.freshReceiveKey();
        final String freshHash = freshKey.toAddress(params).toString();
        log.info("******** fresh hash: {}", freshHash);
        BitcoinjBlockchainServiceImpl.HASH_STACK.push(freshHash);

        log.info("******** Coins on central wallet to me {} from me {}",  valueSentToMe, valueSentFromMe);
        final String receiverHash = getReceiverHash(tx);
        final String senderHash = getSenderHash(tx);
        if (receiverHash == null) {
            return;
        }
        try {

            final Transaction pending = transactionService.getByReceiverHash(receiverHash);
            if (pending == null) {
                log.debug("******** receiver hash {} does not exist", receiverHash);
                return;
            }
            if (!Transaction.Status.PENDING.equals(pending.getStatus())) {
                log.debug("******** transaction {} with {} has status {}", pending.getId(), receiverHash, pending.getStatus());
                return;
            }
            final BigDecimal balance = new BigDecimal(valueSentToMe.subtract(valueSentFromMe).longValue()).divide(new BigDecimal(Constants.MILLI_TO_SATOSHI));
            BigDecimal fees = new BigDecimal(valueSentFromMe.getValue());
            if (fees.floatValue() > 0) {
                fees = fees.divide(new BigDecimal(Constants.MILLI_TO_SATOSHI));
            }
            final BigDecimal amount = balance.add(fees);
            log.info("transaction: amount {} = {} + {} (balance + fees)", pending.getAmount(), balance, fees);
            if (amount.compareTo(pending.getAmount()) > 0) {
                log.error("Transaction {}: paid amount {} greater than expected amont {}", receiverHash, amount, pending.getAmount());
            }
            if (amount.compareTo(pending.getAmount()) < 0) {
                log.error("Transaction {}: paid amount {} less than expected amont {}", receiverHash, amount, pending.getAmount());
            }
            final Transaction transaction = TransactionTO
                    .from(pending)
                    .transactionHash(tx.getHashAsString())
                    .balance(balance)
                    .blockchainFees(fees)
                    .senderHash(senderHash)
                    .receiverHash(null)
                    .QR(null)
                    .status(Transaction.Status.ACCEPTED)
                    .build();
            transactionService.save(transaction.getId(), transaction);
            peerGroup.broadcastTransaction(tx);
        } catch (final ObjectNotFoundException e) {
            log.error("transaction not found", e);
        } catch (final IllegalTransactionStateException e) {
            log.error("illegal transaction state", e);
        } catch (final InsufficientFundsException e) {
            log.error("insufficient funds", e);
        } catch (final Exception e) {
            log.error("Unexpected error", e);
        }
        saveWallet(wallet);
    }

    private void saveWallet(final org.bitcoinj.core.Wallet wallet) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            wallet.saveToFileStream(outputStream);
            walletService.save(WalletTO.from(centralWallet).content(DatatypeConverter.printBase64Binary(outputStream.toByteArray())).build());
        } catch (final Exception e) {
            log.error("error adding wallet {}", e);
        }
    }

    private String getSenderHash(final org.bitcoinj.core.Transaction tx) {
        for (final TransactionOutput out : tx.getOutputs() ) {
            final String hash = new Script(out.getScriptBytes()).getToAddress(params).toString();
            if (!BitcoinjBlockchainServiceImpl.HASH_LIST.contains(hash)) {
                return hash;
            }
        }
        return null;
    }

    private String getReceiverHash(final org.bitcoinj.core.Transaction tx) {
        for (final TransactionOutput out : tx.getOutputs() ) {
            final String hash = new Script(out.getScriptBytes()).getToAddress(params).toString();
            if (BitcoinjBlockchainServiceImpl.HASH_LIST.contains(hash)) {
                return hash;
            }
        }
        return null;
    }

}
