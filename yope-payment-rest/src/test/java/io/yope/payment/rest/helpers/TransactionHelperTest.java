package io.yope.payment.rest.helpers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import io.yope.payment.blockchain.bitcoinj.Constants;
import io.yope.payment.rest.ServerConfiguration;
import lombok.extern.slf4j.Slf4j;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class TransactionHelperTest {

    @Mock
    private ServerConfiguration configuration;

    @InjectMocks
    TransactionHelper helper;

    BigDecimal balance = new BigDecimal(10.029356);

    @Test
    public void testGenerateImage() throws Exception {
        Mockito.when(configuration.getImageAbsolutePath()).thenReturn("http://localhost:8080/images/");
        Mockito.when(configuration.getImageFolder()).thenReturn("target/images");
        final BigDecimal amount = balance.setScale(5, RoundingMode.FLOOR).add(TransactionHelper.BLOCKCHAIN_FEES);
        log.debug("amount {} {} {}", balance, amount, amount.divide(Constants.MILLI_BITCOINS));
        final String qrCode =
                helper.generateImageUrl("miKBDPKoxqfrcLLafBLyhqeAsmMCB7i5SW", amount);
        assertNotNull(qrCode);
        log.debug("url {}", qrCode);

        final File image = new File("target/images", qrCode.substring(qrCode.lastIndexOf("/")+1, qrCode.length()));
        log.debug("image {}", image.getPath());
        assertTrue(image.getPath(), image.exists());

    }
}