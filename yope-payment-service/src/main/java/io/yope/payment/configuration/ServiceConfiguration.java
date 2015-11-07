/**
 *
 */
package io.yope.payment.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author mgerardi
 *
 */
@ComponentScan(basePackages = {
        "io.yope.payment.services",
        "io.yope.payment.schedulers"
        })
@Configuration
@Slf4j
public class ServiceConfiguration {
        @PostConstruct
        public void init() {
                log.info("init Service Configuration");
        }

}
