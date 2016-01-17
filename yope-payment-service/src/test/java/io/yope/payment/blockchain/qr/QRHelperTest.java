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

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.configuration.ServerConfiguration;
import io.yope.payment.domain.QRImage;
import io.yope.payment.qr.QRHelper;
import lombok.extern.slf4j.Slf4j;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class QRHelperTest {

    @Mock
    private ServerConfiguration configuration;

    @Mock
    private BlockChainService blockChainService;


    @InjectMocks
    QRHelper helper;

    BigDecimal balance = new BigDecimal(10.029356);

    @Test
    public void testGenerateImage() throws Exception {
        Mockito.when(this.configuration.getImageAbsolutePath()).thenReturn("http://localhost:8080/images/");
        Mockito.when(this.configuration.getImageFolder()).thenReturn("target/images");
        Mockito.when(this.blockChainService.generateCentralWalletHash()).thenReturn("miKBDPKoxqfrcLLafBLyhqeAsmMCB7i5SW");
        final BigDecimal amount = this.balance.setScale(5, RoundingMode.FLOOR).add(new BigDecimal("0.1"));
        log.debug("amount {} {} {}", this.balance, amount, amount.divide(QRHelper.MILLI_BITCOINS));
        final QRImage qrCode = this.helper.getQRImage(amount);
        assertNotNull(qrCode);
        final String url = qrCode.getImageUrl();
        log.debug("url {}", url);
        final File image = new File("target/images", url.substring(url.lastIndexOf("/")+1, url.length()));
        log.debug("image {}", image.getPath());
        assertTrue(image.getPath(), image.exists());

    }
}