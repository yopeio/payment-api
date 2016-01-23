/**
 *
 */
package io.yope.payment.configuration;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.qr.QRHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mgerardi
 *
 */
@ComponentScan(basePackages = {
        "io.yope.payment.services",
        "io.yope.payment.schedulers"
        })
@Configuration
@EnableConfigurationProperties
@Slf4j
public class ServiceConfiguration {

        @PostConstruct
        public void init() {
                log.info("init Service Configuration");
        }

        @ConfigurationProperties(prefix = "server")
        @Bean
        public ServerConfiguration serverConfiguration() {
            return new ServerConfiguration();
        }

        @Bean
        public QRHelper qrHelper(final ServerConfiguration serverConfiguration, final BlockChainService blockChainService) {
            return new QRHelper(serverConfiguration);
        }

}
