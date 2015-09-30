/**
 *
 */
package io.yope.payment.services;

import java.util.List;

import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.ObjectNotFoundException;

/**
 * @author mgerardi
 *
 */
public interface WalletService {

    /**
     * creates a wallet.
     * @param wallet the wallet to be created
     * @return a wallet with a proper id
     */
    Wallet create(Wallet wallet);

    /**
     * retrieves a wallet by id
     * @param id the id of the wallet
     * @return a wallet or {@literal null} if none found
     */
    Wallet getById(Long id);

    /**
     * retrieves a wallet by the hash
     * @param hash the hash of the wallet
     * @return a wallet or {@literal null} if none found
     */
    Wallet getByHash(String hash);

    /**
     * update a wallet.
     * @param wallet the wallet modifications
     * @param id the id of the wallet
     * @return the new wallet
     * @throws ObjectNotFoundException if no wallet with the given id is found
     */
    Wallet update(Long id, Wallet wallet) throws ObjectNotFoundException;

    /**
     * deletes a wallet by id
     * @param id the id of the wallet
     * @return the deleted wallet.
     * @throws ObjectNotFoundException if no wallet with the given id is found
     */
    Wallet delete(Long id) throws ObjectNotFoundException;

    /**
     * retrieves a list of wallets owned to an account.
     * @param accountId the id of the account.
     * @return a list of wallet
     */
    List<Wallet> get(Long accountId);

}
