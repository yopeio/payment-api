package io.yope.payment.services;

import java.util.List;

import io.yope.payment.domain.Transaction;
import io.yope.payment.domain.Transaction.Direction;
import io.yope.payment.exceptions.ObjectNotFoundException;

public interface TransactionService {

    /**
     * creates a new transaction;
     * @param transaction the transaction to be created; it has to contains the wallets from and to which to perform the transaction.
     * @return the new transaction with an id
     * @throws ObjectNotFoundException if no walllet with the hash provided is found
     */
    Transaction create(Transaction transaction) throws ObjectNotFoundException;

    /**
     * retrieves a transaction with the given id.
     * @param id the id of the transactions;
     * @return a transaction of {@literal null} if none found
     */
    Transaction get(Long id);

    /**
     * retrieves a list of transactions according to different filters
     * @param walletHash the hash of the wallet to whom the transaction belong to - Mandatory
     * @param reference the text contained into the reference field -m optional
     * @param direction the direction of the transaction - optional
     *          - OUT for all the transactions going out of the given wallet
     *          - IN for all the transactions going in to the given wallet
     *          - BOTH or null for all the transactions
     * @return a list of transactions from/to the given wallet
     * @throws ObjectNotFoundException if no wallet with {@code walletHash} found
     */
    List<Transaction> getForWallet(String walletHash, String reference, Direction direction) throws ObjectNotFoundException;

    /**
     * retrieves a list of transactions from all the wallets owned by an account, according to different filters
     * @param accountId the id of the account to whom the transaction refers to - Mandatory
     * @param reference the text contained into the reference field -m optional
     * @param direction the direction of the transaction - optional
     *          - OUT to get the transactions going out of the given wallet
     *          - IN to get the transactions going in to the given wallet
     *          - BOTH or null for all the transactions
     * @return a list of transactions from/to the given account
     * @throws ObjectNotFoundException if no account with {@code accountId} found
     */
    List<Transaction> getForAccount(Long accountId, String reference, Direction direction) throws ObjectNotFoundException;



}
