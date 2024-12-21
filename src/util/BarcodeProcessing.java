package util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


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

        return DataConversion.binaryBitmapToBufferedImage(bitmap);
    }
}
