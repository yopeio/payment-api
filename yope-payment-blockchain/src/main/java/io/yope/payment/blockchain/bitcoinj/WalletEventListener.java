package io.yope.payment.blockchain.bitcoinj;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.transferobjects.TransactionTO;
import io.yope.payment.exceptions.IllegalTransactionStateException;
import io.yope.payment.exceptions.InsufficientFundsException;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.services.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class WalletEventListener extends AbstractWalletEventListener {

    private final PeerGroup peerGroup;
    private final NetworkParameters params;
    private final TransactionService transactionService;
    private final BlockchainSettings settings;


    @Override
    public synchronized void onCoinsReceived(final org.bitcoinj.core.Wallet wallet,
                                             final org.bitcoinj.core.Transaction tx, final Coin prevBalance,
                                             final Coin newBalance) {
        super.onCoinsReceived(wallet, tx, prevBalance, newBalance);
        final Coin valueSentToMe = tx.getValueSentToMe(wallet);
        final Coin valueSentFromMe = tx.getValueSentFromMe(wallet);

        final DeterministicKey freshKey = wallet.freshReceiveKey();
        final String freshHash = freshKey.toAddress(params).toString();
        log.info("******** fresh hash: {}", freshHash);
        BitcoinjBlockchainServiceImpl.HASH_STACK.push(freshHash);

        log.info("******** Coins on central wallet to me {} from me {}",  valueSentToMe, valueSentFromMe);
        final String receiverHash = getReceiverHash(tx);
        final String senderHash = getSenderHash(tx);
        try {
            final Transaction pending = transactionService.getByReceiverHash(receiverHash);
            if (pending == null) {
                log.error("******** receiver hash {} does not exist", receiverHash);
                return;
            }
            if (!Transaction.Status.PENDING.equals(pending.getStatus())) {
                log.error("******** transaction {} with {} has status {}", pending.getId(), receiverHash, pending.getStatus());
                return;
            }
            final BigDecimal balance = new BigDecimal(valueSentToMe.subtract(valueSentFromMe).longValue()).divide(Constants.MILLI_TO_SATOSHI);
            BigDecimal fees = new BigDecimal(valueSentFromMe.getValue());
            if (fees.floatValue() > 0) {
                fees = fees.divide(Constants.MILLI_TO_SATOSHI);
            }
            log.info("transaction: balance {} amount {} fees {} ", balance, pending.getAmount(), fees);
            if (balance.compareTo(pending.getAmount()) > 0) {
                log.info("Transaction {}: paid amount {} greater than expected amont {}", receiverHash, balance, pending.getAmount());
            }
            if (balance.compareTo(pending.getAmount()) < 0) {
                log.error("Transaction {}: paid amount {} less than expected amont {}", receiverHash, balance , pending.getAmount());
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
        } catch (final ObjectNotFoundException e) {
            log.error("transaction not found", e);
        } catch (final IllegalTransactionStateException e) {
            log.error("illegal transaction state", e);
        } catch (final InsufficientFundsException e) {
            log.error("insufficient funds", e);
        }
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            peerGroup.broadcastTransaction(tx);
            wallet.saveToFileStream(outputStream);
        } catch (final Exception e) {
            log.error("error adding wallet {}", e);
        }
    }

    @Override
    public void onTransactionConfidenceChanged(final org.bitcoinj.core.Wallet wallet,
                                               final org.bitcoinj.core.Transaction tx) {
        final String transactionHash = tx.getHashAsString();
        final Transaction loaded = transactionService.getByTransactionHash(transactionHash);
        if (loaded == null) {
            log.error("transaction hash {} does not exist", transactionHash);
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
