package io.yope.payment.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAutoConfiguration
@EnableScheduling
@ComponentScan({
    "io.yope.payment.configuration",
    "io.yope.configuration",
    "io.yope.payment.rest.resources",
    })
public class YopePaymentApplication {

    public static void main(final String[] args) throws Exception {
        SpringApplication.run(YopePaymentApplication.class, args);
    }
}