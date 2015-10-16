/**
 *
 */
package io.yope.payment.blockchain.bitcoinj;

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
public class BlockchainSettings {

    @Getter
    @Setter
    private int confirmations;

}
