package io.yope.payment.blockchain.qr;

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

import io.yope.payment.configuration.ServerConfiguration;
import io.yope.payment.domain.QRImage;
import io.yope.payment.qr.QRHelper;
import lombok.extern.slf4j.Slf4j;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class QRHelperTest {

    @Mock
    private ServerConfiguration configuration;

    @InjectMocks
    QRHelper helper;

    BigDecimal balance = new BigDecimal(10.029356);

    @Test
    public void testGenerateImage() throws Exception {
        Mockito.when(configuration.getImageAbsolutePath()).thenReturn("http://localhost:8080/images/");
        Mockito.when(configuration.getImageFolder()).thenReturn("target/images");
        final BigDecimal amount = balance.setScale(5, RoundingMode.FLOOR).add(new BigDecimal("0.1"));
        log.debug("amount {} {} {}", balance, amount, amount.divide(QRHelper.MILLI_BITCOINS));
        final QRImage qrCode = helper.getQRImage(amount, "miKBDPKoxqfrcLLafBLyhqeAsmMCB7i5SW");
        assertNotNull(qrCode);
        final String url = qrCode.getImageUrl();
        log.debug("url {}", url);
        final File image = new File("target/images", url.substring(url.lastIndexOf("/")+1, url.length()));
        log.debug("image {}", image.getPath());
        assertTrue(image.getPath(), image.exists());
    }
}