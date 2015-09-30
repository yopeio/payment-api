package io.yope.payment.blockchain.bitcoinj;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableAutoConfiguration
//@Profile("with-testnet")
public class BitcoinjTestNetConfiguration {
    @Bean(name="testNetParams")
    public NetworkParameters getTestNetParam() {
        return TestNet3Params.get();
    }
}
