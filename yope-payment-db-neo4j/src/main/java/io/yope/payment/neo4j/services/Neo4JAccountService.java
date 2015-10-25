/**
 *
 */
package io.yope.payment.neo4j.services;


import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.yope.payment.db.services.AccountDbService;
import io.yope.payment.db.services.WalletDbService;
import io.yope.payment.domain.Account;
import io.yope.payment.domain.Account.Status;
import io.yope.payment.domain.Wallet;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.neo4j.domain.Neo4JAccount;
import io.yope.payment.neo4j.repositories.AccountRepository;

/**
 * @author massi
 *
 */
@Service
@Transactional(value="neo4jTransactionManager", propagation = Propagation.REQUIRED)
public class Neo4JAccountService implements AccountDbService, InitializingBean {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private WalletDbService walletService;

    @Autowired
    private Neo4jTemplate template;


    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.AccountService#create(io.yope.payment.domain.Account)
     */
    @Override
    @Transactional(value="neo4jTransactionManager", propagation = Propagation.REQUIRED)
    public Account create(final Account account, final Wallet... wallets) {
        for (final Wallet wallet: wallets) {
            if (wallet == null) {
                continue;
            }
            account.getWallets().add(walletService.save(wallet));
        }
        if (account.getFirstName().equals("wrong")) {
            throw new IllegalArgumentException();
        }
        return accountRepository.save(Neo4JAccount.from(account).registrationDate(System.currentTimeMillis()).build()).toAccount();
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.AccountService#getById(java.lang.Long)
     */
    @Override
    public Account getById(final Long id) {
        final Neo4JAccount account =  accountRepository.findOne(id);
        return account == null? null : account.toAccount();
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.AccountService#update(java.lang.Long, io.yope.payment.domain.Account)
     */
    @Override
    public Account update(final Long id, final Account account) throws ObjectNotFoundException {
        if (getById(id) == null) {
            throw new ObjectNotFoundException(MessageFormat.format("Account with id {0} Not Found", id));
        }
        return accountRepository.save(Neo4JAccount.from(account).modificationDate(System.currentTimeMillis()).id(id).build()).toAccount();
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.AccountService#delete(java.lang.Long)
     */
    @Override
    public Account delete(final Long id) throws ObjectNotFoundException{
        final Account account = getById(id);
        if (account == null) {
            throw new ObjectNotFoundException(MessageFormat.format("Account with id {0} Not Found", id));
        }
        final Neo4JAccount toDelete = Neo4JAccount.from(account).modificationDate(System.currentTimeMillis()).status(Status.DEACTIVATED).build();
        return accountRepository.save(toDelete).toAccount();
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.AccountService#getAccounts()
     */
    @Override
    public List<Account> getAccounts() {
        return Lists.newArrayList(accountRepository.findAll()).stream().map(a -> a.toBuilder().wallets(Sets.newHashSet()).build().toAccount()).collect(Collectors.toList());
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        template.query("MATCH (n:Account) SET n:`_Neo4JAccount`", null).finish();
        template.query("MATCH (n:Wallet) SET n:`_Neo4JWallet`", null).finish();
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.AccountService#getByEmail(java.lang.String)
     */
    @Override
    public Account getByEmail(final String email) {
        final Neo4JAccount account = accountRepository.findByEmail(email);
        return account == null? null : account.toAccount();
    }


    @Override
    public List<Account> getByType(final Account.Type type) {
        return accountRepository.findByType(type.name()).stream().map(a -> a.toAccount()).collect(Collectors.toList());
    }

    @Override
    public boolean exists(final Long accountId) {
        return accountRepository.exists(accountId);
    }

}
