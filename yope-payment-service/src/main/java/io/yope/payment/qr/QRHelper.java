package io.yope.payment.qr;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import io.yope.payment.blockchain.BlockChainService;
import io.yope.payment.blockchain.BlockchainException;
import io.yope.payment.configuration.ServerConfiguration;
import io.yope.payment.domain.QRImage;
import io.yope.payment.exceptions.ObjectNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class QRHelper {

    public static final BigDecimal MILLI_BITCOINS = new BigDecimal("1000");

    private static final int QR_WIDTH = 300;

    private static final int QR_HEIGHT = 300;

    private final ServerConfiguration serverConfiguration;

    private final BlockChainService blockChainService;

    public QRImage getQRImage(final BigDecimal amount) throws ObjectNotFoundException, BlockchainException {
        final String hash = this.getHash();
        final String url = this.generateImageUrl(hash, amount);
        return QRImage.builder()
                .amount(amount)
                .hash(hash)
                .imageUrl(url)
                .build();
    }

    private String generateImageUrl(final String hash, final BigDecimal amount) {
        final String code = this.generateCode(hash, amount);
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new QRCodeWriter().encode(code, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);
            final BufferedImage bufferedImage = this.Process2(MatrixToImageWriter.toBufferedImage(bitMatrix), hash, amount);
            final String imageName = hash+".png";
            final File image = new File(this.getImageFolder(), imageName);
            ImageIO.write(bufferedImage, "png", image);
            return this.serverConfiguration.getImageAbsolutePath() + imageName;
        } catch (final WriterException e) {
            log.error("creating image", e);
        } catch (final IOException e) {
            log.error("saving image", e);
        }
        return null;
    }

    protected BufferedImage Process2(final BufferedImage image, final String hash, final BigDecimal amount){
        final Graphics2D gO = image.createGraphics();
        gO.setColor(Color.black);
        gO.setFont(new Font(Font.DIALOG_INPUT, Font.PLAIN, 10));
        final String text = hash + " " + amount.setScale(3, RoundingMode.CEILING) + "mBTC";
        gO.drawString(text, 17, 25);
        return image;
    }

    private File getImageFolder() {
        final File folder = new File(this.serverConfiguration.getImageFolder());
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    private String generateCode(final String hash, final BigDecimal amount) {
        return "bitcoin:" + hash + "?amount=" + amount.divide(MILLI_BITCOINS);
    }

    private String getHash() throws BlockchainException {
        return this.blockChainService.generateCentralWalletHash();
    }



}
