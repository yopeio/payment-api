/**
 *
 */
package io.yope.payment.blockchain;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author massi
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BlockchainSettings {

    private int confirmations;

    private BigDecimal fees;

    private BigDecimal feesThreshold;

    private String walletFolder;

    private String walletName;

    private String adminUsername;

    private String adminPassword;
}
