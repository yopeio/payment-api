/**
 *
 */
package io.yope.payment.domain.transferobjects;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

/**
 * @author massi
 *
 */
@Getter
@Builder
public class QRImage {

    private final String hash;

    private final BigDecimal amount;

    private final String imageUrl;

}
