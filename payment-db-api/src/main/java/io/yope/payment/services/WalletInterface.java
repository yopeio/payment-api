/**
 *
 */
package io.yope.payment.services;

import java.util.List;

import io.yope.payment.domain.Wallet;

/**
 * @author mgerardi
 *
 */
public interface WalletInterface {

    /**
     * creates a wallet.
     * @param wallet the wallet to be created
     * @return a wallet with a proper id
     */
    Wallet create(Wallet wallet);

    /**
     * retrieves a wallet by id
     * @param id the id of the wallet
     * @return a wallet
     */
    Wallet getById(Long id);

    /**
     * retrieves a wallet by the hash
     * @param hash the hash of the wallet
     * @return a wallet
     */
    Wallet getByHash(String hash);

    /**
     * update a wallet.
     * @param wallet the wallet modifications
     * @param id the id of the wallet
     * @return the new wallet
     */
    Wallet update(Long id, Wallet wallet);

    /**
     * deletes a wallet by id
     * @param id the id of the wallet
     * @return the deleted wallet.
     */
    Wallet delete(Long id);

    /**
     * retrieves a list of wallets owned to an account.
     * @param accountId the id of the account.
     * @return a list of wallet
     */
    List<Wallet> get(Long accountId);

}
