package io.yope.payment.blockchain;

import io.yope.payment.domain.Transaction;

/**
 * Blockchain service.
 */
public interface BlockChainService {

    String send(Transaction transaction) throws BlockchainException;

    /**
     * generated a new hash value from the central wallet.
     * @return
     * @throws BlockchainException
     */
    String generateCentralWalletHash() throws BlockchainException;

}
