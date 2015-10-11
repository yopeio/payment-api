package io.yope.payment.blockchain.bitcoinj;

import java.io.File;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.WalletService;

@Configuration
@EnableAutoConfiguration
public class BitcoinjConfiguration {

    @Autowired
    private Environment environment;

    private static final String DEFAULT_LOCATION = "yope-payment-blockchain/src/main/resources/blockstores";

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
                new File(getBlockstoresLocation(), blockstore));
    }

    private String getBlockstoresLocation() {
        return environment.getProperty("yope.blockstores.location", DEFAULT_LOCATION);
    }

    @Bean
    public PeerGroup getPeers(final NetworkParameters params,final BlockChain chain) {
        return new PeerGroup(params, chain);
    }

    @Bean
    public BlockChainService getBlockchainService(final NetworkParameters params,
                                                  final BlockChain blockChain,
                                                  final PeerGroup peerGroup,
                                                  final WalletService walletService,
                                                  final AccountService accountService
                                                  ){

        final BitcoinjBlockchainServiceImpl blockChainService =
                new BitcoinjBlockchainServiceImpl(params, blockChain, peerGroup);
        /*
         * TODO
         * remove code
         * we do not need to init the wallets for the users.
         * if external, they have been already initialized into blockchain
         * if internal, they will not be seen by the blockchain
         *
        List<Account> accounts = accountService.getAccounts();
        List<Wallet> wallets = new ArrayList<Wallet>();
        for (Account account : accounts) {
            wallets.addAll(walletService.get(account.getId()));
        }

        blockChainService.init(wallets);
        */
        return blockChainService;
    }





}
