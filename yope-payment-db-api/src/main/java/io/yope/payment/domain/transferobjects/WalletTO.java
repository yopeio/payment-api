/**
 *
 */
package io.yope.payment.domain.transferobjects;

import java.math.BigDecimal;

import io.yope.payment.domain.Wallet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

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

    @Wither private String content;

    @Wither private String privateKey;

    public static WalletTO.WalletTOBuilder from(final Wallet wallet) {
        return WalletTO.builder()
                .balance(wallet.getBalance())
                .type(wallet.getType())
                .creationDate(wallet.getCreationDate())
                .modificationDate(wallet.getModificationDate())
                .description(wallet.getDescription())
                .hash(wallet.getHash())
                .id(wallet.getId())
                .status(wallet.getStatus())
                .name(wallet.getName())
                .privateKey(wallet.getPrivateKey())
                .content(wallet.getContent());
    }

}
