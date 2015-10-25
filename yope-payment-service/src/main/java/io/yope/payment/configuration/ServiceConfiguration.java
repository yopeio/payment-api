/**
 *
 */
package io.yope.payment.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author mgerardi
 *
 */
@ComponentScan(basePackages = {
        "io.yope.payment.services",
        "io.yope.payment.schedulers"
        })
@Configuration
public class ServiceConfiguration {

}
