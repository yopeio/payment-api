/**
 *
 */
package io.yope.payment.domain;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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
@Builder(builderClassName="Builder", toBuilder=true)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
@ToString(of = {"name", "balance", "availableBalance"})
public class Wallet {

    public enum Status {
        ACTIVE,
        DELETED
    }

    public enum Type {
        EXTERNAL,
        TRANSIT,
        INTERNAL;
    }


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

}
