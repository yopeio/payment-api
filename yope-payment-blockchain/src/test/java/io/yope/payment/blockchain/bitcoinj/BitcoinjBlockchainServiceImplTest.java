/**
 *
 */
package io.yope.payment.blockchain.bitcoinj;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.beust.jcommander.internal.Lists;

import io.yope.payment.blockchain.BlockchainException;
import io.yope.payment.db.services.AccountDbService;
import io.yope.payment.db.services.WalletDbService;
import io.yope.payment.domain.Account;
import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;
import io.yope.payment.transaction.services.TransactionStateService;

/**
 * @author mgerardi
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class BitcoinjBlockchainServiceImplTest {

    private final NetworkParameters params = TestNet3Params.get();

    @Mock
    private TransactionStateService transactionService;

    @Mock
    private WalletDbService walletService;

    @Mock
    private AccountDbService accountService;

    BitcoinjBlockchainServiceImpl service;

    @Mock
    private Account account;

    private List<Wallet> wallets = Lists.newArrayList();

    @Mock
    private Wallet wallet;

    @Mock
    private Transaction transaction;

    static int counter = 0;

    private BlockStore blockStore;

    @Before
    public void setUp() throws Exception {

        this.blockStore = new SPVBlockStore(this.params, new File("tbtc_blockstore"));
        final BlockChain chain = new BlockChain(new Context(this.params), this.blockStore);

        final PeerGroup peerGroup = new PeerGroup(this.params, chain);
        final BlockchainSettings settings = new BlockchainSettings(1, BigDecimal.ZERO, BigDecimal.TEN, "target", "central.wallet", "admin", "password");
        this.service = new BitcoinjBlockchainServiceImpl(this.params, chain, peerGroup, this.transactionService, this.walletService, this.accountService, settings);
        this.service.init();

        this.wallets.add(this.wallet);
        when(this.accountService.getByEmail("admin")).thenReturn(this.account);
        when(this.account.getWallets()).thenReturn(this.wallets);
        when(this.transactionService.getByReceiverHash(anyString())).thenReturn(this.transaction, this.transaction);
        when(this.wallet.toBuilder()).thenReturn(Wallet.builder());
        when(this.wallet.getContent()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                counter ++;
                if (counter == 1) {
                    return FileUtils.readFileToString(new File("src/test/resources/content.cnt"));
                }
                return FileUtils.readFileToString(new File("src/test/resources/content1.cnt"));
            }

        });
    }

    @After
    public void after() throws BlockStoreException {
        this.blockStore.close();
    }

    @Test
    public void testGenerateCentralWalletHashWithTransaction() throws BlockchainException {
        when(this.transactionService.getByReceiverHash(anyString())).thenReturn(null, this.transaction, null);
        when(this.walletService.save(any(Wallet.class))).thenAnswer(new Answer<Wallet>() {
            @Override
            public Wallet answer(final InvocationOnMock invocation) throws Throwable {
                final Wallet wallet = invocation.getArgumentAt(0, Wallet.class);
                FileUtils.write(new File("src/test/resources/content1.cnt"), wallet.getContent());
                return wallet;
            }

        });
        final String hash1 = this.service.generateCentralWalletHash();
        final String hash2 = this.service.generateCentralWalletHash();
        Assert.assertNotSame(hash1, hash2);
    }

    @Test
    public void testGenerateCentralWalletHash() throws BlockchainException {
        when(this.transactionService.getByReceiverHash(anyString())).thenReturn(null);
        when(this.walletService.save(any(Wallet.class))).thenAnswer(new Answer<Wallet>() {
            @Override
            public Wallet answer(final InvocationOnMock invocation) throws Throwable {
                final Wallet wallet = invocation.getArgumentAt(0, Wallet.class);
                FileUtils.write(new File("src/test/resources/content1.cnt"), wallet.getContent());
                return wallet;
            }

        });
        final String hash1 = this.service.generateCentralWalletHash();
        final String hash2 = this.service.generateCentralWalletHash();
        Assert.assertFalse(hash1.equals(hash2));
    }

}
