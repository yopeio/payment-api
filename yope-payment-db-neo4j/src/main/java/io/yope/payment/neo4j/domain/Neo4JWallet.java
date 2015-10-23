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
@Builder(builderClassName="Builder", toBuilder=true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(of = {"name", "balance", "availableBalance"})
@NodeEntity
public class Neo4JWallet {

    @GraphId
    private Long id;

    @Indexed
    private String name;

    private String walletHash;

    private BigDecimal balance;

    private BigDecimal availableBalance;

    private Wallet.Status status;

    private String description;

    private Long creationDate;

    private Long modificationDate;

    private Wallet.Type type;

    private String content;

    private String privateKey;

    public static Neo4JWallet.Builder from(final Wallet wallet) {
        return Neo4JWallet.builder()
                .balance(wallet.getBalance())
                .availableBalance(wallet.getAvailableBalance())
                .type(wallet.getType())
                .creationDate(wallet.getCreationDate())
                .modificationDate(wallet.getModificationDate())
                .description(wallet.getDescription())
                .walletHash(wallet.getWalletHash())
                .id(wallet.getId())
                .status(wallet.getStatus())
                .name(wallet.getName())
                .privateKey(wallet.getPrivateKey())
                .content(wallet.getContent());
    }

    public Wallet toWallet() {
        return Wallet.builder()
                .balance(getBalance())
                .availableBalance(getAvailableBalance())
                .type(getType())
                .creationDate(getCreationDate())
                .modificationDate(getModificationDate())
                .description(getDescription())
                .walletHash(getWalletHash())
                .id(getId())
                .status(getStatus())
                .name(getName())
                .privateKey(getPrivateKey())
                .content(getContent()).build();
    }

}
