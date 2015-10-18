package io.yope.payment.blockchain.bitcoinj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.bind.DatatypeConverter;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.store.UnreadableWalletException;

import com.google.common.collect.Lists;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.blockchain.BlockchainException;
import io.yope.payment.domain.Account;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.WalletTO;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Bitcoinj client implementation.
 */
@Slf4j
@AllArgsConstructor
public class BitcoinjBlockchainServiceImpl implements BlockChainService {

    public static List<String> HASH_LIST = Lists.newArrayList();

    public static Stack<String> HASH_STACK = new Stack<String>();

    private final NetworkParameters params;

    private final BlockChain chain;

    private final PeerGroup peerGroup;

    private final TransactionService transactionService;

    private final AccountService accountService;

    private final BlockchainSettings settings;

    public void init(final Wallet wallet) {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            peerGroup.addPeerDiscovery(new DnsDiscovery(params));
            peerGroup.setBloomFilterFalsePositiveRate(0.00001);
            try {
                registerInBlockchain(org.bitcoinj.core.Wallet.loadFromFileStream(
                        new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(wallet.getContent()))));
                peerGroup.startAsync();
                peerGroup.downloadBlockChain();
            } catch (final UnreadableWalletException e) {
                log.error("wallet {} cannot be registered to the chain", wallet.getWalletHash(), e);
            }
        });
    }

    @Override
    public Wallet register() throws BlockchainException {
        final org.bitcoinj.core.Wallet btcjWallet = new org.bitcoinj.core.Wallet(params);
        final DeterministicKey freshKey = btcjWallet.freshReceiveKey();
        btcjWallet.allowSpendingUnconfirmedTransactions();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            btcjWallet.saveToFileStream(outputStream);
        } catch (final IOException e) {
            throw new BlockchainException(e);
        }
        registerInBlockchain(btcjWallet);

        final WalletTO wallet = WalletTO.builder().walletHash(freshKey.toAddress(params).toString()).
                content(DatatypeConverter.printBase64Binary(outputStream.toByteArray())).
                privateKey(freshKey.getPrivateKeyEncoded(params).toString()).build();
        return wallet;
    }

    @Override
    public void send(final Transaction transaction) throws BlockchainException {
        try {
            final long satoshi = transaction.getAmount().multiply(BigDecimal.valueOf(Constants.MILLI_TO_SATOSHI)).longValue();
            final Coin value = Coin.valueOf(satoshi);
            final org.bitcoinj.core.Wallet sender = centralWallet();
            sender.allowSpendingUnconfirmedTransactions();
            registerInBlockchain(sender);
            final Address receiver = new Address(params, transaction.getDestination().getWalletHash());
            sender.sendCoins(peerGroup, receiver, value);
        } catch (final UnreadableWalletException e) {
            throw new BlockchainException(e);
        } catch (final InsufficientMoneyException e) {
            throw new BlockchainException(e);
        } catch (final AddressFormatException e) {
            throw new BlockchainException(e);
        }
    }

    private void registerInBlockchain(final org.bitcoinj.core.Wallet wallet) {
        log.info("******** register {} in blockchain", wallet.toString());
        chain.addWallet(wallet);
        peerGroup.addWallet(wallet);
        final WalletEventListener walletEventListener =
                new WalletEventListener(peerGroup,params, transactionService, settings);
        wallet.addEventListener(walletEventListener);
    }

    @Override
    public String generateCentralWalletHash() throws BlockchainException {
        try {
            final org.bitcoinj.core.Wallet receiver = centralWallet();
            String freshHash = null;
            if (HASH_STACK.isEmpty()) {
                final DeterministicKey freshKey = receiver.freshReceiveKey();
                freshHash = freshKey.toAddress(params).toString();
            } else {
                freshHash = HASH_STACK.pop();
            }
            log.info("******** fresh hash: {}", freshHash);
            HASH_LIST.add(freshHash);
            return freshHash;
        } catch (final UnreadableWalletException e) {
            log.error("error during hash generation", e);
        }
        return null;
    }

    private org.bitcoinj.core.Wallet centralWallet() throws UnreadableWalletException {
        final Account admin = accountService.getByEmail(BitcoinjConfiguration.ADMIN_EMAIL);
        final Wallet centralWallet = admin.getWallets().iterator().next();
        return org.bitcoinj.core.Wallet.loadFromFileStream(
                new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(centralWallet.getContent())));
    }

}
