package io.yope.payment.mock.services;

import io.yope.payment.domain.Transaction;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.services.TransactionService;

import java.util.List;

/**
 * Created by enrico.mariotti on 30/09/2015.
 */
public class TransactionServiceMock implements TransactionService {
    @Override
    public Transaction create(Transaction transaction) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public Transaction get(Long id) {
        return null;
    }

    @Override
    public List<Transaction> getForWallet(String walletHash, String reference, Transaction.Direction direction) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public List<Transaction> getForAccount(Long accountId, String reference, Transaction.Direction direction) throws ObjectNotFoundException {
        return null;
    }
}
