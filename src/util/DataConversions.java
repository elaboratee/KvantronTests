package util;

import com.google.zxing.BinaryBitmap;
import org.opencv.core.Mat;

import java.awt.*;
import java.awt.image.BufferedImage;

public class DataConversions {
    public static BufferedImage matToBufferedImage(Mat mat) {
        int type;
        if (mat.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (mat.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        } else {
            throw new IllegalArgumentException("Не поддерживаемое количество каналов матрицы: " + mat.channels());
        }

        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();
        byte[] data = new byte[width * height * channels];

        mat.get(0, 0, data);
        BufferedImage bufferedImage = new BufferedImage(width, height, type);
        bufferedImage.getRaster().setDataElements(0, 0, width, height, data);
        return bufferedImage;
    }

    public static BufferedImage binaryBitmapToBufferedImage(BinaryBitmap bitmap) {
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int luminance = bitmap.getBlackMatrix().get(x, y) ? 0 : 255;
                    image.setRGB(x, y, new Color(luminance, luminance, luminance).getRGB());
                }
            }
            return image;
        } catch (Exception e) {
            System.err.println("Ошибка преобразования BinaryBitmap: " + e.getMessage());
            return null;
        }
    }
}
