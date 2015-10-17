/**
 *
 */
package io.yope.payment.domain.transferobjects;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.yope.payment.domain.Wallet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

/**
 * @author mgerardi
 *
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
@ToString(of = {"name", "balance", "availableBalance"})
public class WalletTO implements Wallet {

    private Long id;

    private String walletHash;

    private String name;

    private BigDecimal balance;

    private BigDecimal availableBalance;

    @JsonIgnore
    private Status status;

    private String description;

    private Long creationDate;

    private Long modificationDate;

    @JsonIgnore
    private Type type;

    @JsonIgnore
    @Wither private String content;

    @JsonIgnore
    @Wither private String privateKey;

    public static WalletTO.WalletTOBuilder from(final Wallet wallet) {
        return WalletTO.builder()
                .balance(wallet.getBalance())
                .availableBalance(wallet.getAvailableBalance())
                .type(wallet.getType())
                .creationDate(wallet.getCreationDate())
                .modificationDate(wallet.getModificationDate())
                .description(wallet.getDescription())
                .walletHash(wallet.getWalletHash())
                .id(wallet.getId())
                .status(wallet.getStatus())
                .name(wallet.getName())
                .privateKey(wallet.getPrivateKey())
                .content(wallet.getContent());
    }

}
