package io.yope.payment.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@ComponentScan({"io.yope.payment.rest.resources", "io.yope.payment.rest.helpers", "io.yope.payment.security.services", "io.yope.payment.security.repositories"})
@Import({
        YopePaymentConfiguration.class,
})

public class YopePaymentApplication {
    public static void main(final String[] args) throws Exception {
        SpringApplication.run(YopePaymentApplication.class, args);
    }
}