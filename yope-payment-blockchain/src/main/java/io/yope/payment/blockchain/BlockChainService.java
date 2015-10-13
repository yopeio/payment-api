package io.yope.payment.blockchain;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;

/**
 * Blockchain service.
 */
public interface BlockChainService {

    Wallet register() throws BlockchainException;

    void send(Transaction transaction) throws BlockchainException;

    /**
     * generated a new hash value for a given wallet content.
     * @param content
     * @return
     * @throws BlockchainException
     */
    String generateHash(byte[] content) throws BlockchainException;

}
