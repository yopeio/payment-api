package io.yope.payment.blockchain.bitcoinj;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.transferobjects.TransactionTO;
import io.yope.payment.exceptions.IllegalTransactionStateException;
import io.yope.payment.exceptions.InsufficientFundsException;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.services.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Slf4j
@AllArgsConstructor
public class WalletEventListener extends AbstractWalletEventListener {

    private PeerGroup peerGroup;
    private NetworkParameters params;
    private TransactionService transactionService;
    private BlockchainSettings settings;


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
        String hash = getHash(tx);
        try {
            Transaction pending = transactionService.getForHash(hash);
            if (pending == null) {
                log.error("transaction hash {} does not exist", hash);
                return;
            }
            BigDecimal amount = new BigDecimal(valueSentToMe.subtract(valueSentFromMe).longValue());
            Transaction transaction = TransactionTO
                    .from(pending)
                    .status(Transaction.Status.ACCEPTED)
                    .acceptedDate(System.currentTimeMillis())
                    .amount(amount)
                    .build();
            transactionService.save(transaction.getId(), transaction);
        } catch (ObjectNotFoundException e) {
            log.error("transaction not found", e);
        } catch (IllegalTransactionStateException e) {
            log.error("illegal transaction state", e);
        } catch (InsufficientFundsException e) {
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
    public void onTransactionConfidenceChanged(org.bitcoinj.core.Wallet wallet,
                                               org.bitcoinj.core.Transaction tx) {
        log.info("-----> confidence changed: {} hash: {}", tx.getHashAsString(), getHash(tx));
        TransactionConfidence confidence = tx.getConfidence();
        log.info("new block depth: {} ", confidence.getDepthInBlocks());
        if (confidence.getDepthInBlocks() >= settings.getConfirmations()) {
            Transaction loaded = transactionService.getForHash(getHash(tx));
            try {
                transactionService.transition(loaded.getId(), Transaction.Status.ACCEPTED);
            } catch (ObjectNotFoundException e) {
                log.error("transaction not found", e);
            } catch (InsufficientFundsException e) {
                log.error("Insufficient money", e);
            } catch (IllegalTransactionStateException e) {
                log.error("Illegal transaction state", e);
            }
        }
    }

    private String getHash(org.bitcoinj.core.Transaction tx) {
        String hash = null;
        for (TransactionOutput out : tx.getOutputs() ) {
            hash = new Script(out.getScriptBytes()).getToAddress(params).toString();
        }
        return hash;
    }

}
