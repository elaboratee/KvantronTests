package util;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class BarcodeLocalization {

    public static void localizeBarcodes(Mat binaryImage, Mat image) {

        // Подготовка структур данных
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.erode(binaryImage, binaryImage, kernel);

        Rect roi;
        Mat subMat;
        int roiSize = 60;
        int roiArea = roiSize * roiSize;

        List<Rect> detectedRegions = new ArrayList<>();

        // Локализация с динамическим подбором размера ядра
        for (int y = 0; y < binaryImage.rows() - roiSize; y += roiSize / 4) {
            for (int x = 0; x < binaryImage.cols() - roiSize; x += roiSize / 3) {
                roi = new Rect(x, y, roiSize, roiSize);
                subMat = binaryImage.submat(roi);

                int blackPixelCount = roiArea - Core.countNonZero(subMat);
                if (countTransitions(subMat) >= 100 && countTransitions(subMat) < 800) {
                    if ((blackPixelCount >= roiArea / 2.5) && (blackPixelCount <= roiArea / 1.5)) {
                        detectedRegions.add(roi);
                    }
                }
            }
        }
        printRectangle(detectedRegions, image);
    }

    public static void printRectangle(List<Rect> rests, Mat image) {
        for (Rect rect : rests) {
            Imgproc.rectangle(
                    image,
                    rect.tl(),
                    rect.br(),
                    new Scalar(0, 255, 0),
                    1
            );
        }
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
