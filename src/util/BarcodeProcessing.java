package util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.*;
import java.awt.image.BufferedImage;

import static util.DataConversion.matToBufferedImage;

public class BarcodeProcessing {
    public static BufferedImage processBarcode(BufferedImage image) {
        BinaryBitmap bitmap;
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            System.out.println("Текст штрих-кода: " + result.getText());
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        return DataConversion.binaryBitmapToBufferedImage(bitmap);
    }
}
