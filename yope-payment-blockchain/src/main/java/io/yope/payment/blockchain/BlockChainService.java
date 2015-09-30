package io.yope.payment.blockchain;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Wallet;

import java.util.List;

/**
 * Blockchain service.
 */
public interface BlockChainService {

    Wallet register() throws BlockchainException;

    void send(Transaction transaction) throws BlockchainException;

}
