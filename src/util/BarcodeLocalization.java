package util;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class BarcodeLocalization {

    private static Mat image;

    public static Mat localizeBarcodes(Mat image) {
        // Подготовка констант
        final int roiSize = 20;
        final int roiArea = roiSize * roiSize;

        // Выполнение локализации
        Rect roi;
        Mat subMat;
        for (int y = 0; y < image.rows() - roiSize; y += roiSize) {
            for (int x = 0; x < image.cols() - roiSize; x += roiSize) {
                roi = new Rect(x, y, roiSize, roiSize);
                subMat = image.submat(roi);

                int blackPixelCount = roiArea - Core.countNonZero(subMat);
                if (countTransitions(subMat) >= 75) {
                    if (blackPixelCount >= roiArea / 2.5) {
                        Imgproc.rectangle(
                                image,
                                new Point(x, y),
                                new Point(x + roiSize, y + roiSize),
                                new Scalar(0, 255, 0),
                                2
                        );
                    }
                }
            }
        }

        return image;
    }

    private static int countTransitions(Mat squareRegion) {
        int transitions = 0;
        for (int y = 0; y < squareRegion.rows() - 1; y++) {
            for (int x = 0; x < squareRegion.cols() - 1; x++) {
                int currentPixel = (int) squareRegion.get(y, x)[0];
                int rightPixel = (int) squareRegion.get(y, x + 1)[0];
                int downPixel = (int) squareRegion.get(y + 1, x)[0];

                if ((currentPixel == 0 && rightPixel == 255) || (currentPixel == 255 && rightPixel == 0)) {
                    transitions++;
                }
                if ((currentPixel == 0 && downPixel == 255) || (currentPixel == 255 && downPixel == 0)) {
                    transitions++;
                }
            }
        }
        return transitions;
    }
}
