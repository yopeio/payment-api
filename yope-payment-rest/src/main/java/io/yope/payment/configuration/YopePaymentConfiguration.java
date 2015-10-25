/**
 *
 */
package io.yope.payment.configuration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author massi
 *
 */
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
public class YopePaymentConfiguration {

    @ConfigurationProperties(prefix = "server")
    @Bean
    public ServerConfiguration serverConfiguration() {
        return new ServerConfiguration();
    }

}
