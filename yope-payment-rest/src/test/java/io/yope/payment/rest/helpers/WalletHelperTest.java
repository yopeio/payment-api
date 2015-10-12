package io.yope.payment.rest.helpers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class WalletHelperTest {


    @InjectMocks
    WalletHelper helper;

    @Test
    public void testGenerateImage() throws Exception {
        Image qrCode =
                helper.generateImage("miKBDPKoxqfrcLLafBLyhqeAsmMCB7i5SW", "test", "reference", "description", new BigDecimal(0.01));
        assertNotNull(qrCode);
        BufferedImage bufferedImage= new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        bufferedImage.getGraphics().drawImage(qrCode, 0, 0, null);
        ImageIO.write(bufferedImage, "jpg", new File("qrcode.jpg"));
    }
}