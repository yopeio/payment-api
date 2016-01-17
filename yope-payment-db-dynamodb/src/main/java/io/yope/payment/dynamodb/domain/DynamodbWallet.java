/**
 *
 */
package io.yope.payment.dynamodb.domain;

import java.math.BigDecimal;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import io.yope.payment.domain.Wallet;
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
@ToString(of = {"name", "balance", "availableBalance"})
@DynamoDBTable(tableName = "Wallet")
public class DynamodbWallet {

    private Long id;

    private Long accountId;

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

    public static DynamodbWallet.Builder from(final Wallet wallet) {
        return DynamodbWallet.builder()
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
                .balance(this.getBalance())
                .availableBalance(this.getAvailableBalance())
                .type(this.getType())
                .creationDate(this.getCreationDate())
                .modificationDate(this.getModificationDate())
                .description(this.getDescription())
                .walletHash(this.getWalletHash())
                .id(this.getId())
                .status(this.getStatus())
                .name(this.getName())
                .privateKey(this.getPrivateKey())
                .content(this.getContent()).build();
    }

}
