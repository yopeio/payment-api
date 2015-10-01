package io.yope.payment.blockchain.bitcoinj;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.domain.Account;
import io.yope.payment.domain.Wallet;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.WalletService;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableAutoConfiguration
public class BitcoinjConfiguration {

    @Autowired
    private Environment environment;

    private static final String DEFAULT_LOCATION = "yope-payment-blockchain/src/main/resources/blockstores";

    @Bean
    public Context getContext(NetworkParameters params ) {
        return new Context(params);
    }

    @Bean
    public BlockChain getChain(NetworkParameters params,
                               SPVBlockStore blockStore,
                               Context context)
            throws BlockStoreException {
        return new BlockChain(context, blockStore);
    }

    @Bean
    public SPVBlockStore getBlockStore(NetworkParameters params)
            throws BlockStoreException {
        String blockstore = params instanceof TestNet3Params ? "tbtc_blockstore" : "main_blockstore";

        return new SPVBlockStore(params,
                new File(getBlockstoresLocation(), blockstore));
    }

    private String getBlockstoresLocation() {
        return environment.getProperty("yope.blockstores.location", DEFAULT_LOCATION);
    }

    @Bean
    public PeerGroup getPeers(NetworkParameters params,BlockChain chain) {
        return new PeerGroup(params, chain);
    }

    @Bean
    public BlockChainService getBlockchainService(NetworkParameters params,
                                                  BlockChain blockChain,
                                                  PeerGroup peerGroup,
                                                  WalletService walletService,
                                                  AccountService accountService
                                                  ){

        BitcoinjBlockchainServiceImpl blockChainService =
                new BitcoinjBlockchainServiceImpl(params, blockChain, peerGroup);
        List<Account> accounts = accountService.getAccounts();
        List<Wallet> wallets = new ArrayList();
        for (Account account : accounts) {
            wallets.addAll(walletService.get(account.getId()));
        }

        blockChainService.init(wallets);
        return blockChainService;
    }





}
