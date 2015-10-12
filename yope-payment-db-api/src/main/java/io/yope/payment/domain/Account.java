/**
 *
 */
package io.yope.payment.domain;

import java.util.Set;

/**
 *
 * @author mgerardi
 *
 */
public interface Account {

    public enum Status {
        ACTIVE,
        DEACTIVATED,
        PENDING,
        SUSPENDED;
    }

    public enum Type {
        SELLER, BUYER, ADMIN;
    }

    Long getId();

    String getEmail();

    Set<Wallet> getWallets();

    String getFirstName();

    String getLastName();

    Status getStatus();

    Type getType();

    Long getRegistrationDate();

    Long getModificationDate();
}
