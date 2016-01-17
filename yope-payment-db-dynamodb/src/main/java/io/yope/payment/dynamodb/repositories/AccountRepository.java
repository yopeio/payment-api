/**
 *
 */
package io.yope.payment.dynamodb.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import io.yope.payment.domain.Account.Type;
import io.yope.payment.dynamodb.domain.DynamodbAccount;

/**
 * @author mgerardi
 *
 */
public interface AccountRepository extends CrudRepository<DynamodbAccount, Long> {

    DynamodbAccount findByEmail(String email);

    List<DynamodbAccount> findByType(Type type);
}
