/**
 *
 */
package io.yope.payment.domain.transferobjects;

import java.math.BigDecimal;

import io.yope.payment.domain.Wallet;
import lombok.Builder;
import lombok.Getter;

/**
 * @author mgerardi
 *
 */
@Builder
@Getter
public class WalletTO implements Wallet {

    private Long id;

    private String hash;

    private BigDecimal balance;

    private Status status;

    private String name;

    private String description;

    private Long creationDate;

    private Long modificationDate;

    private Type type;

    private byte[] content;

    private String privateKey;

}
