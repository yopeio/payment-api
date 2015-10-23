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
import io.yope.payment.domain.Account;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.Wallet;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.TransactionService;
import io.yope.payment.services.WalletService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Bitcoinj client implementation.
 */
@Slf4j
@AllArgsConstructor
public class BitcoinjBlockchainServiceImpl implements BlockChainService {

    protected static final String ADMIN_EMAIL = "wallet@yope.io";

    public static List<String> HASH_LIST = Lists.newArrayList();

    public static Stack<String> HASH_STACK = new Stack<String>();

    private final NetworkParameters params;

    private final BlockChain chain;

    private final PeerGroup peerGroup;

    private final TransactionService transactionService;

    private final WalletService walletService;

    private final AccountService accountService;

    private final BlockchainSettings settings;

    public void init() {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            peerGroup.addPeerDiscovery(new DnsDiscovery(params));
            peerGroup.setBloomFilterFalsePositiveRate(0.00001);
            final Wallet central = getCentralWallet();
            try {
                log.info("central wallet hash: {}", central.getWalletHash());
                registerInBlockchain(org.bitcoinj.core.Wallet.loadFromFileStream(
                        new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(central.getContent()))), central);
                peerGroup.startAsync();
                peerGroup.downloadBlockChain();
            } catch (final UnreadableWalletException e) {
                log.error("wallet {} cannot be registered to the chain", central.getWalletHash(), e);
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

        final Wallet wallet = Wallet.builder().walletHash(freshKey.toAddress(params).toString()).
                content(DatatypeConverter.printBase64Binary(outputStream.toByteArray())).
                privateKey(freshKey.getPrivateKeyEncoded(params).toString()).build();
        return wallet;
    }

    private Wallet getCentralWallet() {
        Account admin = accountService.getByEmail(ADMIN_EMAIL);
        if (admin == null) {
            try {
                final Wallet inBlockChain = register();
                final Wallet central = Wallet.builder()
                        .content(inBlockChain.getContent())
                        .walletHash(inBlockChain.getWalletHash())
                        .type(Wallet.Type.EXTERNAL)
                        .status(Wallet.Status.ACTIVE)
                        .name("central")
                        .description("central")
                        .balance(BigDecimal.ZERO)
                        .build();
                final Account adm = Account.builder()
                        .email(ADMIN_EMAIL)
                        .firstName("admin")
                        .lastName("admin")
                        .type(Account.Type.ADMIN)
                        .wallets(Lists.newArrayList())
                        .build();
                admin = accountService.create(adm, central);
            } catch (final BlockchainException e) {
                log.error("error during blockchain registration", e);
            }
        }
        return admin.getWallets().iterator().next();
    }

    @Override
    public void send(final Transaction transaction) throws BlockchainException {
        try {
            final long satoshi = transaction.getAmount().multiply(Constants.MILLI_TO_SATOSHI).longValue();
            final Coin value = Coin.valueOf(satoshi);
            final org.bitcoinj.core.Wallet sender = centralWallet();
            sender.allowSpendingUnconfirmedTransactions();
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

    private void registerInBlockchain(final org.bitcoinj.core.Wallet wallet, final Wallet centralWallet) {
        log.info("******** register {} in blockchain", wallet.toString());
        chain.addWallet(wallet);
        peerGroup.addWallet(wallet);
        final WalletEventListener walletEventListener =
                new WalletEventListener(peerGroup,params, transactionService, settings, walletService, centralWallet);
        wallet.addEventListener(walletEventListener);
    }

    @Override
    public String generateCentralWalletHash() throws BlockchainException {
        try {
            String freshHash = null;
            Transaction transaction = null;
            do {
                freshHash = getFreshHash(freshHash);
                transaction = transactionService.getByReceiverHash(freshHash);
            } while (transaction != null);
            return freshHash;
        } catch (final UnreadableWalletException e) {
            log.error("error during hash generation", e);
            throw new BlockchainException(e);
        }
    }

    private String getFreshHash(final String previous) throws UnreadableWalletException, BlockchainException {
        final org.bitcoinj.core.Wallet receiver = centralWallet();
        if (HASH_STACK.isEmpty()) {
            final DeterministicKey freshKey = receiver.freshReceiveKey();
            final String hash = freshKey.toAddress(params).toString();
            if (hash.equals(previous)) {
                throw new BlockchainException("cannot generate new hash");
            }
            HASH_LIST.add(hash);
            return hash;
        }
        return HASH_STACK.pop();
    }

    private org.bitcoinj.core.Wallet centralWallet() throws UnreadableWalletException {
        final Account admin = accountService.getByEmail(ADMIN_EMAIL);
        final Wallet centralWallet = admin.getWallets().iterator().next();
        return org.bitcoinj.core.Wallet.loadFromFileStream(
                new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(centralWallet.getContent())));
    }

}
