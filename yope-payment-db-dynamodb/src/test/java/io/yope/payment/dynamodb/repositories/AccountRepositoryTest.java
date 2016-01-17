/**
 *
 */
package io.yope.payment.dynamodb.repositories;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import io.yope.payment.domain.Account.Type;
import io.yope.payment.dynamodb.configuration.SpringDataDynamoDemoConfig;
import io.yope.payment.dynamodb.domain.DynamodbAccount;;

/**
 * @author mgerardi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class,classes={SpringDataDynamoDemoConfig.class})
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class AccountRepositoryTest {

    @Autowired private AccountRepository repository;
    private DynamodbAccount account;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.account = DynamodbAccount.builder()
                .id(1L)
                .email("test@test.tst")
                .type(Type.SELLER)
                .firstName("test")
                .lastName("test")
                .build();
    }

    /**
     * Test method for {@link io.yope.payment.dynamodb.repositories.AccountRepository#findByEmail(java.lang.String)}.
     */
    @Test
    public void testFindByEmail() {
        this.repository.save(this.account);
        final DynamodbAccount account = this.repository.findOne(1L);
        Assert.assertNotNull(account);
    }

    /**
     * Test method for {@link io.yope.payment.dynamodb.repositories.AccountRepository#findByType(io.yope.payment.domain.Account.Type)}.
     */
    @Test
    public void testFindByType() {
        fail("Not yet implemented");
    }

}
