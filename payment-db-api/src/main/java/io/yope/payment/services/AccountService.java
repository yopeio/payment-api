/**
 *
 */
package io.yope.payment.services;

import java.util.List;

import io.yope.payment.domain.Account;

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
    Account create(Account account);

    /**
     * retrieves a user by their id.
     * @param id
     * @return the requested user
     */
    Account getById(Long id);

    /**
     * updates a user.
     * @param id the id of the user.
     * @param account the account containing the modifications.
     * @return the updated user
     */
    Account update(Long id, Account account);

    /**
     * deletes a user.
     * @param id the id of the user.
     * @return the deleted user
     */
    Account delete(Long id);

    /**
     * retrieves all the users.
     * @return the list of users.
     */
    List<Account> getAccounts();

}
