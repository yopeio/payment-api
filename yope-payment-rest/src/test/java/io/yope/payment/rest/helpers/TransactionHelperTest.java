package io.yope.payment.rest.helpers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import io.yope.payment.rest.ServerConfiguration;
import lombok.extern.slf4j.Slf4j;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class TransactionHelperTest {

    @Mock
    private ServerConfiguration configuration;

    @InjectMocks
    TransactionHelper helper;

    @Test
    public void testGenerateImage() throws Exception {
        Mockito.when(configuration.getImageAbsolutePath()).thenReturn("http://localhost:8080/images/");
        Mockito.when(configuration.getImageFolder()).thenReturn("target/images");

        final String qrCode =
                helper.generateImageUrl("miKBDPKoxqfrcLLafBLyhqeAsmMCB7i5SW", new BigDecimal(0.01));
        assertNotNull(qrCode);
        log.debug("url {}", qrCode);

        final File image = new File("target/images", qrCode.substring(qrCode.lastIndexOf("/")+1, qrCode.length()));
        log.debug("image {}", image.getPath());
        assertTrue(image.getPath(), image.exists());

    }
}