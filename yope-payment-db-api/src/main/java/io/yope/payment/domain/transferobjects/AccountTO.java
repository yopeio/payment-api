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
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString(of= {"email", "type"})
public class AccountTO implements Account {

    private Long id;

    private String email;

    private Set<Wallet> wallets;

    private String firstName;

    private String lastName;

    private Status status;

    private Type type;

    private Long registrationDate;

    private Long modificationDate;
}
