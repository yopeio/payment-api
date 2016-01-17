/**
 *
 */
package io.yope.payment.neo4j.domain;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import io.yope.payment.domain.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

/**
 * @author mgerardi
 *
 */
@Builder(builderClassName="Builder", toBuilder=true)
@Wither
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(of= {"type", "email"}, includeFieldNames = false)
@NodeEntity
public class Neo4JAccount {

    private Account.Type type;

    @GraphId
    private Long id;

    @Indexed(unique=true)
    private String email;

    @RelatedTo(type="OWN")
    @Fetch
    private Set<Neo4JWallet> wallets;

    private String firstName;

    private String lastName;

    private Account.Status status;

    private Long registrationDate;

    private Long modificationDate;

    public static Neo4JAccount.Builder from(final Account account) {
        final Builder builder = Neo4JAccount.builder()
                .email(account.getEmail())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .id(account.getId())
                .status(account.getStatus())
                .type(account.getType())
                .modificationDate(account.getModificationDate())
                .registrationDate(account.getRegistrationDate());
        if (account.getWallets() != null) {
            builder.wallets(account.getWallets().stream().map(t -> Neo4JWallet.from(t).build()).collect(Collectors.toSet()));
        }
        return builder;
    }

    public Account toAccount() {
        final Account.Builder builder = Account.builder()
                .email(this.getEmail())
                .firstName(this.getFirstName())
                .lastName(this.getLastName())
                .id(this.getId())
                .status(this.getStatus())
                .type(this.getType())
                .modificationDate(this.getModificationDate())
                .registrationDate(this.getRegistrationDate());
        if (this.getWallets() != null) {
            builder.wallets(this.getWallets().stream().map(w -> w.toWallet()).collect(Collectors.toList()));
        }
        return builder.build();
    }

}
