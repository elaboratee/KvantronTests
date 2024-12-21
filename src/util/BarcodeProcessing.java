package util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.image.BufferedImage;


public class BarcodeProcessing {

    public static BufferedImage processBarcode(BufferedImage image,
                                               int minX, int minY,
                                               int width, int height) {

        LuminanceSource source = new BufferedImageLuminanceSource(image, minX, minY, width, height);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = new MultiFormatReader().decode(bitmap);
            System.out.println("Текст штрих-кода: " + result.getText());
        } catch (NotFoundException e) {
            System.err.println("Штрих-код не найден");
        }

        return DataConversions.binaryBitmapToBufferedImage(bitmap);
    }
}
