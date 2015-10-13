/**
 *
 */
package io.yope.payment.domain;

import java.math.BigDecimal;

/**
 * @author mgerardi
 *
 */
public interface Wallet {

    enum Status {
        ACTIVE,
        DELETED,
        PENDING;
    }

    public enum Type {
        EXTERNAL,
        INTERNAL;
    }

    Long getId();

    String getHash();

    BigDecimal getBalance();

    Status getStatus();

    String getName();

    String getDescription();

    Long getCreationDate();

    Long getModificationDate();

    Type getType();

    String getContent();

    String getPrivateKey();

}
