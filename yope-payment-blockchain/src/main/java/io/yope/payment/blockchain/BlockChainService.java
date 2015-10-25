package io.yope.payment.blockchain;

import java.io.IOException;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;

/**
 * Blockchain service.
 */
public interface BlockChainService {

    String send(Transaction transaction) throws BlockchainException;

    Wallet saveCentralWallet(org.bitcoinj.core.Wallet wallet) throws IOException;

    /**
     * generated a new hash value from the central wallet.
     * @return
     * @throws BlockchainException
     */
    String generateCentralWalletHash() throws BlockchainException;

}
