package io.yope.payment.blockchain.bitcoinj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Wallet.BalanceType;
import org.bitcoinj.core.Wallet.SendResult;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.store.UnreadableWalletException;

import com.google.common.collect.Lists;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.blockchain.BlockchainException;
import io.yope.payment.db.services.AccountDbService;
import io.yope.payment.db.services.WalletDbService;
import io.yope.payment.domain.Account;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;
import io.yope.payment.transaction.services.TransactionStateService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Bitcoinj client implementation.
 */
@Slf4j
@AllArgsConstructor
public class BitcoinjBlockchainServiceImpl implements BlockChainService {

    private final NetworkParameters params;

    private final BlockChain chain;

    private final PeerGroup peerGroup;

    private final TransactionStateService transactionService;

    private final WalletDbService walletService;

    private final AccountDbService accountService;

    private final BlockchainSettings settings;

    public void init() {
        final ExecutorService executorService = Executors
                .newSingleThreadExecutor();
        executorService.execute(() -> {
            this.peerGroup.addPeerDiscovery(new DnsDiscovery(this.params));
            this.peerGroup.setBloomFilterFalsePositiveRate(0.00001);
            try {
                final File walletFolder = new File(this.settings.getWalletFolder());
                if (!walletFolder.exists()) {
                    FileUtils.forceMkdir(walletFolder);
                }
                final Wallet central = this.getCentralWallet();
                log.info("central wallet hash: {}", central.getWalletHash());
                this.registerInBlockchain(org.bitcoinj.core.Wallet
                        .loadFromFileStream(new ByteArrayInputStream(
                                DatatypeConverter.parseBase64Binary(
                                        central.getContent()))));
                this.peerGroup.startAsync();
                this.peerGroup.downloadBlockChain();
            } catch (final UnreadableWalletException e) {
                log.error("wallet cannot be registered to the chain", e);
            } catch (final IOException e) {
                log.error("Error", e);
            }
        });
    }

    private org.bitcoinj.core.Wallet createBlockchainWallet()
            throws IOException {
        final org.bitcoinj.core.Wallet wallet = new org.bitcoinj.core.Wallet(
                this.params);
        wallet.allowSpendingUnconfirmedTransactions();
        return wallet;
    }

    private Wallet getCentralWallet() throws IOException {
        final org.bitcoinj.core.Wallet blockchainWallet = this.getOrRegister();
        Wallet central = this.saveCentralWallet(blockchainWallet);
        if (central == null) {
            central = this.createCentralWallet(blockchainWallet);
        }
        return central;
    }

    private Wallet createCentralWallet(
            final org.bitcoinj.core.Wallet blockchainWallet)
                    throws IOException {
        final Wallet centralWallet = this.saveCentralWallet(Wallet.builder().build(),
                blockchainWallet);
        this.accountService.create(Account.builder()
                .email(this.settings.getAdminUsername()).firstName("admin")
                .lastName("admin").type(Account.Type.ADMIN)
                .wallets(Lists.newArrayList(centralWallet)).build());
        return centralWallet;

    }

    private Wallet saveCentralWallet(final Wallet wallet,
            final org.bitcoinj.core.Wallet blockchainWallet)
                    throws IOException {
        if (wallet == null) {
            return null;
        }
        final DeterministicKey freshKey = blockchainWallet.freshReceiveKey();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blockchainWallet.saveToFileStream(outputStream);
        final String walletHash = freshKey.toAddress(this.params).toString();
        final String privatekey = freshKey.getPrivateKeyEncoded(this.params)
                .toString();
        final String content = DatatypeConverter
                .printBase64Binary(outputStream.toByteArray());
        final BigDecimal balance = new BigDecimal(
                blockchainWallet.getBalance(BalanceType.ESTIMATED).value)
                        .divide(Constants.MILLI_TO_SATOSHI);
        final BigDecimal availableBalance = new BigDecimal(
                blockchainWallet.getBalance(BalanceType.AVAILABLE).value)
                        .divide(Constants.MILLI_TO_SATOSHI);
        ;
        final Wallet central = wallet.toBuilder().content(content)
                .walletHash(walletHash).privateKey(privatekey)
                .type(Wallet.Type.EXTERNAL).status(Wallet.Status.ACTIVE)
                .name(this.settings.getWalletName())
                .description(FilenameUtils.concat(this.settings.getWalletFolder(), this.settings.getWalletName()))
                .balance(balance).availableBalance(availableBalance).build();
        this.saveToFile(blockchainWallet);
        return this.walletService.save(central);
    }

    private org.bitcoinj.core.Wallet getOrRegister() throws IOException {
        org.bitcoinj.core.Wallet wallet = this.loadFromFile();
        if (wallet == null) {
            wallet = this.createBlockchainWallet();
        }
        return wallet;
    }

    private void saveToFile(final org.bitcoinj.core.Wallet wallet) {
        final File walletFile = new File(this.settings.getWalletFolder(), this.settings.getWalletName());
        try {
            wallet.saveToFile(walletFile);
        } catch (final IOException e) {
            log.error("Error saving to file:", e);
        }
    }

    private org.bitcoinj.core.Wallet loadFromFile() {
        final File walletFile = new File(this.settings.getWalletFolder(), this.settings.getWalletName());
        if (walletFile.exists()) {
            //backup(walletFile);
            try {
                return org.bitcoinj.core.Wallet.loadFromFile(walletFile);
            } catch (final UnreadableWalletException e) {
                log.error("Error loading from file:", e);
            }
        }
        return null;
    }

    private void backup(final File walletFile) {
        try {
            final String bakWalletName = this.settings.getWalletName() + "." + System.currentTimeMillis();
            FileUtils.copyFile(walletFile, new File(this.settings.getWalletFolder(), bakWalletName));
        } catch (final IOException e) {
            log.error("Error saving backup file:", e);
        }
    }

    @Override
    public Wallet saveCentralWallet(
            final org.bitcoinj.core.Wallet blockchainWallet)
                    throws IOException {
        final Wallet centralWallet = this.getWallet();
        return this.saveCentralWallet(centralWallet, blockchainWallet);
    }

    private Wallet getWallet() {
        final Account admin = this.accountService
                .getByEmail(this.settings.getAdminUsername());
        if (admin == null) {
            return null;
        }
        final List<Wallet> wallets = admin.getWallets();
        if (CollectionUtils.isEmpty(wallets)) {
            return null;
        }
        return wallets.get(0);
    }

    @Override
    public String send(final Transaction transaction)
            throws BlockchainException {
        try {
            final long satoshi = transaction.getAmount()
                    .multiply(Constants.MILLI_TO_SATOSHI).longValue();
            final Coin value = Coin.valueOf(satoshi);
            final org.bitcoinj.core.Wallet sender = this.centralWallet();
            sender.allowSpendingUnconfirmedTransactions();
            final Address receiver = new Address(this.params,
                    transaction.getDestination().getWalletHash());
            final SendResult result = sender.sendCoins(this.peerGroup, receiver,
                    value);
            result.broadcastComplete.get();
            return result.tx.getHashAsString();
        } catch (final UnreadableWalletException e) {
            throw new BlockchainException(e);
        } catch (final InsufficientMoneyException e) {
            throw new BlockchainException(e);
        } catch (final AddressFormatException e) {
            throw new BlockchainException(e);
        } catch (final InterruptedException e) {
            throw new BlockchainException(e);
        } catch (final ExecutionException e) {
            throw new BlockchainException(e);
        }
    }

    private void registerInBlockchain(final org.bitcoinj.core.Wallet wallet) {
        log.info("******** register {} in blockchain", wallet.toString());
        this.chain.addWallet(wallet);
        this.peerGroup.addWallet(wallet);
        final WalletEventListener walletEventListener = new WalletEventListener(
                this.peerGroup, this.params, this.transactionService, this.settings,
                this);
        wallet.addEventListener(walletEventListener);
    }

    @Override
    public String generateCentralWalletHash() throws BlockchainException {
        try {
            final org.bitcoinj.core.Wallet receiver = this.centralWallet();
            String freshHash = null;
            Transaction transaction = null;
            do {
                freshHash = this.getFreshHash(freshHash, receiver);
                transaction = this.transactionService.getByReceiverHash(freshHash);
            } while (transaction != null);
            this.saveCentralWallet(receiver);
            return freshHash;
        } catch (final UnreadableWalletException e) {
            log.error("error during hash generation", e);
            throw new BlockchainException(e);
        } catch (final IOException e) {
            log.error("cannot save central wallet", e);
            throw new BlockchainException(e);
        }
    }

    private String getFreshHash(final String previous, final org.bitcoinj.core.Wallet receiver)
            throws UnreadableWalletException, BlockchainException, IOException {
        final DeterministicKey freshKey = receiver.freshReceiveKey();
        final String hash = freshKey.toAddress(this.params).toString();
        if (hash.equals(previous)) {
            throw new BlockchainException("cannot generate new hash");
        }
        return hash;
    }

    private org.bitcoinj.core.Wallet centralWallet()
            throws UnreadableWalletException {
        final Wallet centralWallet = this.getWallet();
        return org.bitcoinj.core.Wallet
                .loadFromFileStream(new ByteArrayInputStream(DatatypeConverter
                        .parseBase64Binary(centralWallet.getContent())));
    }

}
