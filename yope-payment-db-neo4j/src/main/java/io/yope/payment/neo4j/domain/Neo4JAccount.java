/**
 *
 */
package io.yope.payment.neo4j.domain;

import java.util.Set;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Wallet;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @author mgerardi
 *
 */
@Getter
@NodeEntity
@Builder
@ToString(of= {"email", "id"}, includeFieldNames = false)
public class Neo4JAccount implements Account {

    @GraphId
    private final Long id;

    @Indexed(unique=true)
    private final String email;

    @RelatedTo(type="OWN")
    private final Set<Wallet> wallets;

    private final String firstName;

    private final String lastName;

    private final Status status;

    private final Type type;

    private final Long registrationDate;

    private final Long modificationDate;

    public static Neo4JAccount.Neo4JAccountBuilder from(final Account account) {
        return Neo4JAccount.builder()
                .email(account.getEmail())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .id(account.getId())
                .status(account.getStatus())
                .modificationDate(account.getModificationDate())
                .registrationDate(account.getRegistrationDate())
                .wallets(account.getWallets());

    }


}
