package io.yope.payment.blockchain.bitcoinj;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@Profile("with-testnet")
public class BitcoinjTestNetConfiguration {
    @Bean(name="testNetParams")
    public NetworkParameters getTestNetParam() {
        return TestNet3Params.get();
    }
}
