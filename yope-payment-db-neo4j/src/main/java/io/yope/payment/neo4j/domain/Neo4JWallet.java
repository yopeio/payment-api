/**
 *
 */
package io.yope.payment.neo4j.domain;

import java.math.BigDecimal;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

import io.yope.payment.domain.Wallet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author mgerardi
 *
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString(of = {"id", "balance"}, includeFieldNames = false)
@NodeEntity
public class Neo4JWallet implements Wallet {

    @GraphId
    private Long id;

    @Indexed(unique = true)
    private String hash;

    private BigDecimal balance;

    private Status status;

    @Indexed
    private String name;

    private String description;

    private Long creationDate;

    private Long modificationDate;

    private Type type;

    public static Neo4JWallet.Neo4JWalletBuilder from(final Wallet wallet) {
        return Neo4JWallet.builder()
                .balance(wallet.getBalance())
                .type(wallet.getType())
                .creationDate(wallet.getCreationDate())
                .modificationDate(wallet.getModificationDate())
                .description(wallet.getDescription())
                .hash(wallet.getHash())
                .id(wallet.getId())
                .status(wallet.getStatus())
                .name(wallet.getName());
    }
}
