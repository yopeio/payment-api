/**
 *
 */
package io.yope.payment.neo4j.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.*;
import org.springframework.data.neo4j.annotation.*;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Wallet;

/**
 * @author mgerardi
 *
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString(of= {"email", "id"}, includeFieldNames = false)
@NodeEntity
public class Neo4JAccount implements Account {

    @GraphId
    private Long id;

    @Indexed(unique=true)
    private String email;

    @RelatedTo(type="OWN")
    @Fetch
    private Set<Neo4JWallet> wallets;

    private String firstName;

    private String lastName;

    private Status status;

    private Type type;

    private Long registrationDate;

    private Long modificationDate;

    public static Neo4JAccount.Neo4JAccountBuilder from(final Account account) {
        Neo4JAccountBuilder builder = Neo4JAccount.builder()
                .email(account.getEmail())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .id(account.getId())
                .status(account.getStatus())
                .modificationDate(account.getModificationDate())
                .registrationDate(account.getRegistrationDate());
        if (account.getWallets() != null) {
            builder.wallets(account.getWallets().stream().map(t -> Neo4JWallet.from(t).build()).collect(Collectors.toSet()));
        }
        return builder;
    }

    @Override
    public Set<Wallet> getWallets() {
        return new HashSet<Wallet>(wallets);
    }
}