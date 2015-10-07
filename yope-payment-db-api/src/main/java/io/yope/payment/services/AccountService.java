/**
 *
 */
package io.yope.payment.services;

import java.util.List;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.ObjectNotFoundException;

/**
 * @author mgerardi
 *
 */
public interface AccountService {

    /**
     * creates an account
     * @param account the account to be created.
     * @return the new account with a new id
     */
    Account create(Account account, Wallet... wallets);

    /**
     * retrieves an account by their id.
     * @param id
     * @return the requested user or {@literal null} if none found;
     */
    Account getById(Long id);

    /**
     * updates an account with a given {@code id}
     * @param id the id of the user.
     * @param account the account containing the modifications.
     * @return the updated user
     * @throws ObjectNotFoundException if no user with {@code id} found
     */
    Account update(Long id, Account account) throws ObjectNotFoundException;

    /**
     * deletes an account with a given {@code id}
     * @param id the id of the user.
     * @return the deleted user
     * @throws ObjectNotFoundException if no user with {@code id} found
     */
    Account delete(Long id) throws ObjectNotFoundException;

    /**
     * retrieves all the users.
     * @return the list of users.
     */
    List<Account> getAccounts();

}
