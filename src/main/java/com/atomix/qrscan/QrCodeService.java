package com.atomix.qrscan;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class QrCodeService {
    public BufferedImage generate(String text, int width, int height) throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
        hints.put(EncodeHintType.MARGIN, 2);
        return MatrixToImageWriter.toBufferedImage(
                new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints));
    }

    public String decode(BufferedImage image) {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.ALSO_INVERTED, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, List.of(BarcodeFormat.QR_CODE));

        BinaryBitmap bitmap = new BinaryBitmap(
                new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        MultiFormatReader reader = new MultiFormatReader();
        reader.setHints(hints);
        try {
            for (int rotation = 0; rotation < 4; rotation++) {
                try {
                    Result result = reader.decodeWithState(bitmap);
                    return result.getText();
                } catch (NotFoundException exception) {
                    if (rotation == 3 || !bitmap.isRotateSupported()) {
                        break;
                    }
                    bitmap = bitmap.rotateCounterClockwise();
                }
            }
            return null;
        } finally {
            reader.reset();
        }
    }
}
