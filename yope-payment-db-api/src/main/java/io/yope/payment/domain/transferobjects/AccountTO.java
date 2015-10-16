/**
 *
 */
package io.yope.payment.domain.transferobjects;

import java.util.Set;

import io.yope.payment.domain.Account;
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
@Builder(builderClassName="Builder")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString(of= {"type", "email"}, includeFieldNames = false)
public class AccountTO implements Account {

    private Type type;

    private Long id;

    private String email;

    private Set<Wallet> wallets;

    private String firstName;

    private String lastName;

    private Status status;

    private Long registrationDate;

    private Long modificationDate;

    public static AccountTO.Builder from(final Account account) {
        return AccountTO.builder()
                .email(account.getEmail())
                .firstName(account.getFirstName())
                .id(account.getId())
                .lastName(account.getLastName())
                .modificationDate(account.getModificationDate())
                .registrationDate(account.getRegistrationDate())
                .wallets(account.getWallets())
                .type(account.getType())
                .status(account.getStatus());
    }

}
