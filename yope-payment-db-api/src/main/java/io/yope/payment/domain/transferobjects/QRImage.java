/**
 *
 */
package io.yope.payment.domain.transferobjects;

import java.awt.Image;
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

    private final String reference;

    private final String name;

    private final String description;

    private final String hash;

    private final BigDecimal amount;

    private final Image image;
}
