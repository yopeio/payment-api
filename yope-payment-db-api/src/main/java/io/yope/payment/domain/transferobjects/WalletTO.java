/**
 *
 */
package io.yope.payment.domain.transferobjects;

import java.math.BigDecimal;

import io.yope.payment.domain.Wallet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Wither;

/**
 * @author mgerardi
 *
 */
@Builder
@Getter
@AllArgsConstructor
public class WalletTO implements Wallet {

    public WalletTO() {
    }

    private Long id;

    private String hash;

    private BigDecimal balance;

    private Status status;

    private String name;

    private String description;

    private Long creationDate;

    private Long modificationDate;

    private Type type;

    @Wither private byte[] content;

    @Wither private String privateKey;

}
