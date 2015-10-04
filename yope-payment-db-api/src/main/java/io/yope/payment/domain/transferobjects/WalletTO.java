/**
 *
 */
package io.yope.payment.domain.transferobjects;

import io.yope.payment.domain.Wallet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.math.BigDecimal;

/**
 * @author mgerardi
 *
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
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

    @Wither private byte[] content;

    @Wither private String privateKey;

}
