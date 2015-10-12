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
     * generated a new hash value for a given wallet.
     * @param wallet
     * @return
     * @throws BlockchainException
     */
    String generateHash(Wallet wallet) throws BlockchainException;

}
