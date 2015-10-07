package io.yope.payment.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;

import io.yope.config.OAuth2ServerConfig;
import io.yope.config.SecurityConfig;
import io.yope.payment.neo4j.config.RestDataConfiguration;
import io.yope.payment.neo4j.config.YopeNeo4jConfiguration;

@Controller
@Configuration
@EnableAutoConfiguration
@ComponentScan({"io.yope.payment.rest.resources", "io.yope.payment.rest.helpers", "io.yope.payment.security.services"})
@Import({
        RestDataConfiguration.class,
        YopeNeo4jConfiguration.class,
        OAuth2ServerConfig.class,
        SecurityConfig.class
//        BitcoinjConfiguration.class,
//        BitcoinjMainNetConfiguration.class,
//        BitcoinjTestNetConfiguration.class,
//        MockConfiguration.class
})

public class YopePaymentApplication {
    public static void main(final String[] args) throws Exception {
        SpringApplication.run(YopePaymentApplication.class, args);
    }
}