package io.yope.payment.blockchain.bitcoinj;


import java.io.File;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.TransactionService;
import io.yope.payment.services.WalletService;


@Configuration
public class BitcoinjConfiguration {


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
                                                  final TransactionService transactionService,
                                                  final WalletService walletService,
                                                  final AccountService accountService,
                                                  final BlockchainSettings settings
                                                  ){

        final BitcoinjBlockchainServiceImpl blockChainService =
                new BitcoinjBlockchainServiceImpl(params, blockChain, peerGroup, transactionService, walletService, accountService, settings);

        blockChainService.init();
        return blockChainService;
    }

}
