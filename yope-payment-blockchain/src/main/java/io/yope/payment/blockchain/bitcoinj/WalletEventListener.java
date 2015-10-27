package io.yope.payment.blockchain.bitcoinj;

import java.math.BigDecimal;
import java.util.List;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.DeterministicKey;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.IllegalTransactionStateException;
import io.yope.payment.exceptions.InsufficientFundsException;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.transaction.services.TransactionStateService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class WalletEventListener extends AbstractWalletEventListener {

    private final PeerGroup peerGroup;
    private final NetworkParameters params;
    private final TransactionStateService transactionService;
    private final BlockchainSettings settings;
    private final BlockChainService blockChainService;

    @Override
    public void onCoinsReceived(final org.bitcoinj.core.Wallet wallet,
            final org.bitcoinj.core.Transaction tx, final Coin prevBalance,
            final Coin newBalance) {
        super.onCoinsReceived(wallet, tx, prevBalance, newBalance);
        log.info("Received coins tx: {}", tx.getHashAsString());
        tx.getConfidence().addEventListener(new ConfidenceListener(transactionService, settings));
        saveWallet(wallet);
        saveNewHash(wallet);
        initializeTransaction(tx, wallet);
    }

    private void initializeTransaction(final org.bitcoinj.core.Transaction tx,
            final org.bitcoinj.core.Wallet wallet) {
        try {
            final Transaction pending = getTransaction(tx.getOutputs(), wallet);
            if (pending == null || !Transaction.Status.PENDING.equals(pending.getStatus())) {
                return;
            }
            final String senderHash = getSenderHash(tx.getOutputs(), wallet);
            final Coin valueSentToMe = tx.getValueSentToMe(wallet);
            final Coin valueSentFromMe = tx.getValueSentFromMe(wallet);
            final BigDecimal balance = new BigDecimal(valueSentToMe.subtract(valueSentFromMe).longValue()).divide(Constants.MILLI_TO_SATOSHI);
            BigDecimal fees = BigDecimal.ZERO;
            if (valueSentFromMe.getValue() > 0) {
                fees = new BigDecimal(valueSentFromMe.getValue()).divide(Constants.MILLI_TO_SATOSHI);
            }
            log.info("transaction: balance {} amount {} fees {} ", balance, pending.getAmount(), fees);
            if (balance.compareTo(pending.getAmount()) > 0) {
                log.warn("***** WARNING *****\n\n              Transaction {}: paid amount {} greater than expected amont {}\n\n", pending.getId(), balance, pending.getAmount());
            }
            if (balance.compareTo(pending.getAmount()) < 0) {
                log.error("***** WARNING *****\n\n              Transaction {}: paid amount {} less than expected amont {}\n\n", pending.getId(), balance , pending.getAmount());
            }
            final Transaction transaction = pending.toBuilder()
                    .transactionHash(tx.getHashAsString())
                    .balance(balance)
                    .blockchainFees(fees)
                    .receiverHash(null)
                    .senderHash(senderHash)
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
        } catch (final Exception e) {
            log.error("Unexpected error", e);
        }
        peerGroup.broadcastTransaction(tx);
    }

    private void saveNewHash(final org.bitcoinj.core.Wallet wallet) {
        final DeterministicKey freshKey = wallet.freshReceiveKey();
        final String freshHash = freshKey.toAddress(params).toString();
        BitcoinjBlockchainServiceImpl.HASH_STACK.push(freshHash);
    }

    private void saveWallet(final org.bitcoinj.core.Wallet wallet) {
        try {
            final Wallet centralWallet = blockChainService.saveCentralWallet(wallet);
            if (centralWallet == null) {
                log.error("central wallet not found!");
            }
        } catch (final Exception e) {
            log.error("error saving wallet", wallet);
            log.error("Error:", e);
        }
    }

    private String getSenderHash(final List<TransactionOutput> outputs, final org.bitcoinj.core.Wallet wallet) {
        for (final TransactionOutput o : outputs) {
            if (o.isMine(wallet)) {
                continue;
            }
            return o.getScriptPubKey().getToAddress(params).toString();
        }
        return null;
    }

    private Transaction getTransaction(final List<TransactionOutput> outputs, final org.bitcoinj.core.Wallet wallet) {
        for (final TransactionOutput o : outputs) {
            if (!o.isMine(wallet)) {
                continue;
            }
            final String hash = o.getScriptPubKey().getToAddress(params).toString();
            final Transaction transaction = transactionService.getByReceiverHash(hash);
            if (transaction != null) {
                return transaction;
            }
        }
        return null;
    }

    @Override
    public void onCoinsSent(final org.bitcoinj.core.Wallet wallet,
            final org.bitcoinj.core.Transaction tx, final Coin prevBalance,
            final Coin newBalance) {
        super.onCoinsSent(wallet, tx, prevBalance, newBalance);
        log.info("Sent coins tx: {}", tx.getHashAsString());
        tx.getConfidence().addEventListener(new ConfidenceListener(transactionService, settings));
        saveWallet(wallet);
    }

}
