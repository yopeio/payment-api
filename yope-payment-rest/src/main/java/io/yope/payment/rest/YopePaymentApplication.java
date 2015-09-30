package io.yope.payment.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;

@Controller
@Configuration
@EnableAutoConfiguration
@ComponentScan("io.yope.payment.blockchain.bitcoinj,io.yope.payment.mock")
public class YopePaymentApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(YopePaymentApplication.class, args);
    }
}