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

        log.info("******** Coins on central wallet to me {} from me {}",  valueSentToMe, valueSentFromMe);
        final String hash = getHash(tx);
        try {
            final Transaction pending = transactionService.getForHash(hash);
            if (pending == null) {
                log.error("transaction hash {} does not exist", hash);
                return;
            }
            final BigDecimal amount = new BigDecimal(valueSentToMe.subtract(valueSentFromMe).longValue());
            final Transaction transaction = TransactionTO
                    .from(pending)
                    .transactionHash(tx.getHashAsString())
                    .status(Transaction.Status.ACCEPTED)
                    .acceptedDate(System.currentTimeMillis())
                    .amount(amount)
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
        log.info("-----> confidence changed: {} hash: {}", tx.getHashAsString(), getHash(tx));
        final TransactionConfidence confidence = tx.getConfidence();
        log.info("new block depth: {} ", confidence.getDepthInBlocks());
        if (confidence.getDepthInBlocks() >= settings.getConfirmations()) {
            final Transaction loaded = transactionService.getForHash(getHash(tx));
            try {
                transactionService.transition(loaded.getId(), Transaction.Status.ACCEPTED);
            } catch (final ObjectNotFoundException e) {
                log.error("transaction not found", e);
            } catch (final InsufficientFundsException e) {
                log.error("Insufficient money", e);
            } catch (final IllegalTransactionStateException e) {
                log.error("Illegal transaction state", e);
            }
        }
    }

    private String getHash(final org.bitcoinj.core.Transaction tx) {
        String hash = null;
        for (final TransactionOutput out : tx.getOutputs() ) {
            hash = new Script(out.getScriptBytes()).getToAddress(params).toString();
        }
        return hash;
    }

}
