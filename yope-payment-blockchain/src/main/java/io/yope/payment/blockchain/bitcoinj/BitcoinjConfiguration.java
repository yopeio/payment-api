package io.yope.payment.blockchain.bitcoinj;


import com.google.common.collect.Sets;
import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.blockchain.BlockchainException;
import io.yope.payment.domain.Account;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.transferobjects.AccountTO;
import io.yope.payment.domain.transferobjects.WalletTO;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.WalletService;
import lombok.extern.slf4j.Slf4j;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Slf4j
@Configuration
@EnableAutoConfiguration
public class BitcoinjConfiguration {

    private static final String ADMIN_EMAIL = "wallet@yope.io";
    private static final String CENTRAL_WALLET_PATH = "centralwallet";
    @Autowired
    private Environment environment;

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


        Wallet central = null;
        byte[] content;
        Account admin = accountService.getByEmail(ADMIN_EMAIL);
        if (admin == null) {
            try {
                Wallet inBlockChain = blockChainService.register();
                writeCentralWallet(inBlockChain);
                central = WalletTO.builder()
//                        .writeCentralWallet(inBlockChain.getContent())
                        .hash(inBlockChain.getHash())
                        .type(Wallet.Type.EXTERNAL)
                        .status(Wallet.Status.ACTIVE)
                        .name("central")
                        .description("central")
                        .balance(BigDecimal.ZERO)
                        .build();
                Account adm = AccountTO.builder()
                        .email(ADMIN_EMAIL)
                        .firstName("admin")
                        .lastName("admin")
                        .type(Account.Type.ADMIN)
                        .wallets(Sets.newLinkedHashSet())
                        .build();
                accountService.create(adm, central);

            } catch (BlockchainException e) {
                log.error("error during blockchain registration", e);
            }
        } else {
            central = admin.getWallets().iterator().next();
        }
        content = readCentralWallet();
        blockChainService.init(central, content);
        log.info("central wallet hash: {}", central.getHash());
        return blockChainService;
    }

    private void writeCentralWallet(Wallet inBlockChain) {
        try {
            FileOutputStream fos = new FileOutputStream(CENTRAL_WALLET_PATH);
            fos.write(inBlockChain.getContent());
            fos.close();
        } catch (IOException e) {
            log.error("error during central wallet generation", e);
        }
    }

    private byte[] readCentralWallet() {
        byte[] data = null;
        this.getClass().getClassLoader()
                .getResourceAsStream(CENTRAL_WALLET_PATH);
        Path path = Paths.get(CENTRAL_WALLET_PATH);
        try {
            data = Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("error during central wallet reading: {}", e.getMessage());
        }
        return data;
    }


}
