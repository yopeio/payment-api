package io.yope.payment.neo4j.services;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import io.yope.payment.db.services.WalletDbService;
import io.yope.payment.domain.Wallet;
import io.yope.payment.domain.Wallet.Status;
import io.yope.payment.exceptions.ObjectNotFoundException;
import io.yope.payment.neo4j.domain.Neo4JWallet;
import io.yope.payment.neo4j.repositories.WalletRepository;

@Service
@Transactional
public class Neo4JWalletService implements WalletDbService {

    @Autowired
    private WalletRepository repository;

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.WalletService#create(io.yope.payment.domain.Wallet)
     */
    @Override
    public Wallet create(final Wallet wallet) {
        return this.repository.save(Neo4JWallet.from(wallet).creationDate(System.currentTimeMillis()).build()).toWallet();
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.WalletService#getById(java.lang.Long)
     */
    @Override
    public Wallet getById(final Long id) {
        final Neo4JWallet wallet = this.repository.findOne(id);
        return wallet == null? null : wallet.toWallet();
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.WalletService#getByHash(java.lang.String)
     */
    @Override
    public Wallet getByWalletHash(final String hash) {
        final Neo4JWallet wallet = this.repository.findByWalletHash(hash);
        return wallet == null? null : wallet.toWallet();
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.WalletService#update(java.lang.Long, io.yope.payment.domain.Wallet)
     */
    @Override
    public Wallet save(final Long id, final Wallet wallet) throws ObjectNotFoundException {
        if (!this.repository.exists(id)) {
            throw new ObjectNotFoundException(id, Wallet.class);
        }
        return this.repository.save(Neo4JWallet.from(wallet).modificationDate(System.currentTimeMillis()).build()).toWallet();
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.WalletService#delete(java.lang.Long)
     */
    @Override
    public Wallet delete(final Long id) throws ObjectNotFoundException {
        final Wallet wallet = this.getById(id);
        if (wallet == null) {
            throw new ObjectNotFoundException(id, Wallet.class);
        }
        return this.repository.save(Neo4JWallet.from(wallet).status(Status.DELETED).modificationDate(System.currentTimeMillis()).build()).toWallet();
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.WalletService#get(java.lang.Long)
     */
    @Override
    public List<Wallet> getWalletsByAccountId(final Long accountId, final Status status) {
        final String statusParam = StringUtils.defaultIfBlank(status==null? null: status.name(), ".*");
        return Lists.newArrayList(this.repository.findAllByOwner(accountId, statusParam)).stream().map(t -> t.toWallet()).collect(Collectors.toList());
    }

    /*
     * (non-Javadoc)
     * @see io.yope.payment.services.WalletService#getByName(java.lang.Long, java.lang.String)
     */
    @Override
    public Wallet getByName(final Long accountId, final String name) {
        final Neo4JWallet wallet = this.repository.findByName(accountId, name);
        return wallet == null? null : wallet.toWallet();
    }

    @Override
    public boolean exists(final Long id) {
        return this.repository.exists(id);
    }

}
