package io.yope.payment.configuration;


import java.io.File;

import javax.annotation.PostConstruct;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.blockchain.BlockchainSettings;
import io.yope.payment.blockchain.bitcoinj.BitcoinjBlockchainServiceImpl;
import io.yope.payment.db.services.AccountDbService;
import io.yope.payment.db.services.WalletDbService;
import io.yope.payment.transaction.services.TransactionStateService;
import lombok.extern.slf4j.Slf4j;


@Configuration
@Slf4j
public class BitcoinjConfiguration {

    @PostConstruct
    public void init() {
        log.info("init Blockchain Service Configuration");
    }


    @ConfigurationProperties(prefix = "blockchain")
    @Bean
    public BlockchainSettings blockchainSettings() {
        return new BlockchainSettings();
    }

    @Bean
    public Context getContext(final NetworkParameters params ) {
        return new Context(params);
    }

    @Bean
    public BlockChain getChain(final NetworkParameters params,
                               final SPVBlockStore blockStore,
                               final Context context)
            throws BlockStoreException {
        return new BlockChain(context, blockStore);
    }

    @Bean
    public SPVBlockStore getBlockStore(final NetworkParameters params)
            throws BlockStoreException {
        final String blockstore = params instanceof TestNet3Params ? "tbtc_blockstore" : "main_blockstore";

        return new SPVBlockStore(params,
                new File(blockstore));
    }


    @Bean
    public PeerGroup getPeers(final NetworkParameters params,final BlockChain chain) {
        return new PeerGroup(params, chain);
    }


    @Bean
    public BlockChainService getBlockchainService(final NetworkParameters params,
                                                  final BlockChain blockChain,
                                                  final PeerGroup peerGroup,
                                                  final TransactionStateService transactionService,
                                                  final WalletDbService walletService,
                                                  final AccountDbService accountService,
                                                  final BlockchainSettings settings
                                                  ){

        final BitcoinjBlockchainServiceImpl blockChainService =
                new BitcoinjBlockchainServiceImpl(params, blockChain, peerGroup, transactionService, walletService, accountService, settings);

        blockChainService.init();
        return blockChainService;
    }

}
