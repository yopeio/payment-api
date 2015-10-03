package io.yope.payment.rest;

import io.yope.payment.blockchain.bitcoinj.BitcoinjConfiguration;
import io.yope.payment.blockchain.bitcoinj.BitcoinjMainNetConfiguration;
import io.yope.payment.blockchain.bitcoinj.BitcoinjTestNetConfiguration;
import io.yope.payment.mock.MockConfiguration;
import io.yope.payment.neo4j.config.RestDataConfiguration;
import io.yope.payment.neo4j.config.YopeNeo4jConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;

@Controller
@Configuration
@EnableAutoConfiguration
@ComponentScan("io.yope.payment.rest.resources, io.yope.payment.blockchain.bitcoinj, io.yope.payment.mock.services")
@Import({
        RestDataConfiguration.class,
        YopeNeo4jConfiguration.class,
        BitcoinjConfiguration.class,
        BitcoinjMainNetConfiguration.class,
        BitcoinjTestNetConfiguration.class,
//        MockConfiguration.class
})

public class YopePaymentApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(YopePaymentApplication.class, args);
    }
}