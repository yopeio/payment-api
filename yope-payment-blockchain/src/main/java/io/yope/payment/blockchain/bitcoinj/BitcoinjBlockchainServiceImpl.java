package io.yope.payment.blockchain.bitcoinj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.blockchain.BlockchainException;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.WalletTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Bitcoinj client implementation.
 */
@Slf4j
@AllArgsConstructor
public class BitcoinjBlockchainServiceImpl implements BlockChainService {

    NetworkParameters params;

    BlockChain chain;

    PeerGroup peerGroup;

    public void init(final List<Wallet> wallets) {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            peerGroup.addPeerDiscovery(new DnsDiscovery(params));
            peerGroup.setBloomFilterFalsePositiveRate(0.00001);

            for (final Wallet wallet : wallets) {
                try {
                    registerInBlockchain(org.bitcoinj.core.Wallet.loadFromFileStream(
                            new ByteArrayInputStream(wallet.getContent())));
                } catch (final UnreadableWalletException e) {
                    log.error("wallet {} cannot be registered to the chain", wallet.getHash());
                }
            }
            peerGroup.startAsync();
            peerGroup.downloadBlockChain();
        });
    }

    @Override
    public Wallet register() throws BlockchainException {
        final org.bitcoinj.core.Wallet btcjWallet = new org.bitcoinj.core.Wallet(params);
        final DeterministicKey freshKey = btcjWallet.freshReceiveKey();
        //todo: how many confirmations does Yope wait?
        btcjWallet.allowSpendingUnconfirmedTransactions();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            btcjWallet.saveToFileStream(outputStream);
        } catch (final IOException e) {
            throw new BlockchainException(e);
        }
        registerInBlockchain(btcjWallet);
        final WalletTO wallet = WalletTO.builder().hash(freshKey.toAddress(params).toString()).
                content(outputStream.toByteArray()).
                privateKey(freshKey.getPrivateKeyEncoded(params).toString()).build();
        return wallet;
    }

    @Override
    public void send(final Transaction transaction) throws BlockchainException {
        try {
            final Coin value = Coin.valueOf(transaction.getAmount().longValue());
            final org.bitcoinj.core.Wallet sender =
                    org.bitcoinj.core.Wallet.loadFromFileStream(
                            new ByteArrayInputStream(transaction.getSource().getContent()));
            sender.allowSpendingUnconfirmedTransactions();
            registerInBlockchain(sender);
            final Address receiver = new Address(params, transaction.getDestination().getHash());
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
        chain.addWallet(wallet);
        peerGroup.addWallet(wallet);
    }

    @Override
    public String generateHash(final Wallet wallet) throws BlockchainException {
        return null;
    }

}
