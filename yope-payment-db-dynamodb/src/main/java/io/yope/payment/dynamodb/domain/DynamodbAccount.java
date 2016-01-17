/**
 *
 */
package io.yope.payment.dynamodb.domain;

import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import io.yope.payment.domain.Account;
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
@lombok.experimental.Wither
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(of= {"type", "email"}, includeFieldNames = false)
@DynamoDBTable(tableName = "Account")
public class DynamodbAccount {

    private Account.Type type;

    private Long id;

    private String email;

    private List<DynamodbWallet> wallets;

    private String firstName;

    private String lastName;

    private Account.Status status;

    private Long registrationDate;

    private Long modificationDate;

    public static DynamodbAccount.Builder from(final Account account) {
        final Builder builder = DynamodbAccount.builder()
                .email(account.getEmail())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .id(account.getId())
                .status(account.getStatus())
                .type(account.getType())
                .modificationDate(account.getModificationDate())
                .registrationDate(account.getRegistrationDate());
        if (account.getWallets() != null) {
            builder.wallets(account.getWallets().stream().map(t -> DynamodbWallet.from(t).accountId(account.getId()).build()).collect(Collectors.toList()));
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
