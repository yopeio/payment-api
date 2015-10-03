/**
 *
 */
package io.yope.payment.domain.transferobjects;

import java.util.Set;

import io.yope.payment.domain.Account;
import io.yope.payment.domain.Wallet;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/**
 * @author mgerardi
 *
 */
@Getter
@Data
@Builder
public class AccountTO implements Account {

    private final Long id;

    private final String email;

    private final Set<Wallet> wallets;

    private final String firstName;

    private final String lastName;

    private final Status status;

    private final Type type;

    private final Long registrationDate;

    private final Long modificationDate;
}
