package util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import gui.LocateCodePanel;

import java.awt.image.BufferedImage;


public class BarcodeProcessing {

    public static void processBarcode(BufferedImage image) {

        BinaryBitmap bitmap = createBitmap(image);

        try {
            Result result = new MultiFormatReader().decode(bitmap);
            LocateCodePanel.getActionLog().append("Текст штрих-кода: " + result.getText() + "\n");
        } catch (NotFoundException e) {
            LocateCodePanel.getActionLog().append("Штрих-код не найден\n");
        }
    }


    public static BufferedImage processBarcode(BufferedImage image,
                                               int minX, int minY,
                                               int width, int height) {

        BinaryBitmap bitmap = createBitmap(image);

        try {
            Result result = new MultiFormatReader().decode(bitmap);
            LocateCodePanel.getActionLog().append("Текст штрих-кода: " + result.getText() + "\n");
        } catch (NotFoundException e) {
            LocateCodePanel.getActionLog().append("Штрих-код не найден\n");
        }

        return DataConversions.binaryBitmapToBufferedImage(bitmap);
    }


    public static BinaryBitmap createBitmap(BufferedImage image) {
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        return new BinaryBitmap(new HybridBinarizer(source));
    }


    public static BinaryBitmap createBitmap(BufferedImage image,
                                            int minX, int minY,
                                            int width, int height) {
        LuminanceSource source = new BufferedImageLuminanceSource(image, minX, minY, width, height);
        return new BinaryBitmap(new HybridBinarizer(source));
    }
}
