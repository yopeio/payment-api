/**
 *
 */
package io.yope.payment.dynamodb.repositories;

import org.springframework.data.repository.CrudRepository;

import io.yope.payment.dynamodb.domain.DynamodbWallet;

/**
 * @author mgerardi
 *
 */
public interface WalletRepository extends CrudRepository<DynamodbWallet, Long> {

    DynamodbWallet findByWalletHash(String hash);

    DynamodbWallet findByName(String name);
}
