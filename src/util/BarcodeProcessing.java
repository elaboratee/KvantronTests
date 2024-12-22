package util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import gui.LocateCodePanel;

import java.awt.image.BufferedImage;


public class BarcodeProcessing {

    public static BufferedImage processBarcode(BufferedImage image,
                                               int minX, int minY,
                                               int width, int height) {

        LuminanceSource source = new BufferedImageLuminanceSource(image, minX, minY, width, height);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = new MultiFormatReader().decode(bitmap);
            LocateCodePanel.getActionLog().append("Текст штрих-кода: " + result.getText());
        } catch (NotFoundException e) {
            LocateCodePanel.getActionLog().append("Штрих-код не найден");
        }

        return DataConversions.binaryBitmapToBufferedImage(bitmap);
    }
}
