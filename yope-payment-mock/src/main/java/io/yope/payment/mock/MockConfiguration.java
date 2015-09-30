package io.yope.payment.mock;

import io.yope.payment.mock.services.AccountServiceMock;
import io.yope.payment.mock.services.TransactionServiceMock;
import io.yope.payment.mock.services.WalletServiceMock;
import io.yope.payment.services.AccountService;
import io.yope.payment.services.TransactionService;
import io.yope.payment.services.WalletService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class MockConfiguration {
    @Bean
    public WalletService getWalletService() {
        return new WalletServiceMock();
    }

    @Bean
    public AccountService getAccountService() {
        return new AccountServiceMock();
    }

    @Bean
    public TransactionService getTransactionService() {
        return new TransactionServiceMock();
    }

}
