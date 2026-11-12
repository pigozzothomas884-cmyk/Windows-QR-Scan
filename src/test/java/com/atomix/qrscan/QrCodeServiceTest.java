package com.atomix.qrscan;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class QrCodeServiceTest {
    private final QrCodeService service = new QrCodeService();

    @Test
    void generatesAndDecodesQrCode() throws Exception {
        String expected = "https://example.com/qr-test";
        BufferedImage qrCode = service.generate(expected, 256, 256);

        assertEquals(expected, service.decode(qrCode));
    }

    @Test
    void decodesRotatedQrCode() throws Exception {
        String expected = "rotated QR code";
        BufferedImage qrCode = service.generate(expected, 256, 256);
        BufferedImage rotatedQrCode = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rotatedQrCode.createGraphics();
        try {
            graphics.rotate(Math.PI / 2, 128, 128);
            graphics.drawImage(qrCode, 0, 0, null);
        } finally {
            graphics.dispose();
        }

        assertEquals(expected, service.decode(rotatedQrCode));
    }

    @Test
    void returnsNullWhenImageHasNoQrCode() {
        BufferedImage blankImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        assertNull(service.decode(blankImage));
    }

    @Test
    void includesApplicationLogoInThePackagedResources() throws Exception {
        try (InputStream imageStream = getClass().getResourceAsStream("/QRScanLogo.png");
             InputStream iconStream = getClass().getResourceAsStream("/InstallerIcon.ico")) {
            assertNotNull(imageStream);
            assertNotNull(iconStream);
            BufferedImage logo = ImageIO.read(imageStream);
            assertNotNull(logo);
            assertEquals(256, logo.getWidth());
            assertEquals(256, logo.getHeight());
            byte[] iconHeader = iconStream.readNBytes(4);
            assertEquals(0, iconHeader[0]);
            assertEquals(0, iconHeader[1]);
            assertEquals(1, iconHeader[2]);
            assertEquals(0, iconHeader[3]);
        }
    }
}
