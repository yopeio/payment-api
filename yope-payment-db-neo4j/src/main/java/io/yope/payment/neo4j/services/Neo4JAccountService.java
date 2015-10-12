/**
 *
 */
package io.yope.payment.neo4j.services;


import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Account.Status;
import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.neo4j.domain.Neo4JAccount;
import io.yope.payment.neo4j.repositories.AccountRepository;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.WalletService;

/**
 * @author massi
 *
 */
@Service
@Transactional("neo4jTransactionManager")
public class Neo4JAccountService implements AccountService, InitializingBean {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private Neo4jTemplate template;


    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.AccountService#create(io.yope.payment.domain.Account)
     */
    @Override
    @Transactional("neo4jTransactionManager")
    public Account create(final Account account, final Wallet... wallets) {
        for (final Wallet wallet: wallets) {
            if (wallet == null) {
                continue;
            }
            account.getWallets().add(this.walletService.create(wallet));
        }
        if (account.getFirstName().equals("wrong")) {
            throw new IllegalArgumentException();
        }
        return this.accountRepository.save(Neo4JAccount.from(account).registrationDate(System.currentTimeMillis()).build());
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.AccountService#getById(java.lang.Long)
     */
    @Override
    public Account getById(final Long id) {
        return this.accountRepository.findOne(id);
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.AccountService#update(java.lang.Long, io.yope.payment.domain.Account)
     */
    @Override
    public Account update(final Long id, final Account account) throws ObjectNotFoundException {
        if (this.getById(id) == null) {
            throw new ObjectNotFoundException(String.valueOf(id), Account.class);
        }
        return this.accountRepository.save(Neo4JAccount.from(account).modificationDate(System.currentTimeMillis()).id(id).build());
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.AccountService#delete(java.lang.Long)
     */
    @Override
    public Account delete(final Long id) throws ObjectNotFoundException{
        final Account account = this.getById(id);
        if (account == null) {
            throw new ObjectNotFoundException(String.valueOf(id), Account.class);
        }
        final Neo4JAccount toDelete = Neo4JAccount.from(account).modificationDate(System.currentTimeMillis()).status(Status.DEACTIVATED).build();
        return this.accountRepository.save(toDelete);
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.AccountService#getAccounts()
     */
    @Override
    public List<Account> getAccounts() {
        return this.accountRepository.findAll().as(List.class);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.template.query("MATCH (n:Account) SET n:`_Neo4JAccount`", null).finish();
        this.template.query("MATCH (n:Wallet) SET n:`_Neo4JWallet`", null).finish();
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.AccountService#getByEmail(java.lang.String)
     */
    @Override
    public Account getByEmail(final String email) {
        return this.accountRepository.findByEmail(email);
    }

}
