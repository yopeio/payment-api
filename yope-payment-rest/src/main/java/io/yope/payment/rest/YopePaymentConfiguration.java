/**
 *
 */
package io.yope.payment.rest;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.yope.config.OAuth2ServerConfig;
import io.yope.config.SecurityConfig;
import io.yope.payment.blockchain.bitcoinj.BitcoinjConfiguration;
import io.yope.payment.blockchain.bitcoinj.BitcoinjMainNetConfiguration;
import io.yope.payment.blockchain.bitcoinj.BitcoinjTestNetConfiguration;
import io.yope.payment.neo4j.config.RestDataConfiguration;
import io.yope.payment.neo4j.config.YopeNeo4jConfiguration;

/**
 * @author massi
 *
 */
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
@Import({
    RestDataConfiguration.class,
    YopeNeo4jConfiguration.class,
    OAuth2ServerConfig.class,
    SecurityConfig.class,
    BitcoinjConfiguration.class,
    BitcoinjMainNetConfiguration.class,
    BitcoinjTestNetConfiguration.class,
    StaticResourceConfiguration.class
})
public class YopePaymentConfiguration {

    @ConfigurationProperties(prefix = "server")
    @Bean
    public ServerConfiguration serverConfiguration() {
        return new ServerConfiguration();
    }
}
