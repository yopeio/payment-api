package io.yope.payment.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;

import io.yope.payment.neo4j.config.RestDataConfiguration;
import io.yope.payment.neo4j.config.YopeNeo4jConfiguration;

@Controller
@Configuration
@EnableAutoConfiguration
@ComponentScan("io.yope.payment.rest.resources")
@Import({
        RestDataConfiguration.class,
        YopeNeo4jConfiguration.class,
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