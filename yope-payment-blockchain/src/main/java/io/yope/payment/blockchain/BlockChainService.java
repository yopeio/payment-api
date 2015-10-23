package io.yope.payment.blockchain;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;

/**
 * Blockchain service.
 */
public interface BlockChainService {

    Wallet register() throws BlockchainException;

    String send(Transaction transaction) throws BlockchainException;

    /**
     * generated a new hash value from the central wallet.
     * @return
     * @throws BlockchainException
     */
    String generateCentralWalletHash() throws BlockchainException;

}
