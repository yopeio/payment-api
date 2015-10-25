/**
 *
 */
package io.yope.payment.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author massi
 *
 */
@ComponentScan(basePackages = {
        "io.yope.payment.transaction.services",
        })
@Configuration
public class TransactionServiceConfiguration {

}
