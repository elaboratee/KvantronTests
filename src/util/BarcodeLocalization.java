package util;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class BarcodeLocalization {

    public static Mat localizeBarcodes(Mat image) {
        // Подготовка структур данных
        Rect roi;
        Mat subMat;
        List<Rect> foundRects = new ArrayList<>();

        // Локализация с динамическим подбором размера ядра
        for (int roiSize = 100, roiArea = roiSize * roiSize; roiSize >= 20; roiSize -= 20) {
            for (int y = 0; y < image.rows() - roiSize; y += roiSize) {
                for (int x = 0; x < image.cols() - roiSize; x += roiSize) {
                    roi = new Rect(x, y, roiSize, roiSize);
                    subMat = image.submat(roi);

                    int blackPixelCount = roiArea - Core.countNonZero(subMat);
                    if (countTransitions(subMat) >= 75) {
                        if (blackPixelCount >= roiArea / 2.5) {
                            if (roiSize == 100) {
                                foundRects.add(roi);
                            } else {
                                int flag = 0;
                                for (Rect rect : foundRects) {
                                    if (rect.contains(new Point(roi.x, roi.y)) ||
                                            rect.contains(new Point(roi.x + roiSize, roi.y + roiSize))) {
                                        flag = 1;
                                        break;
                                    }
                                }
                                if (flag == 0) {
                                    foundRects.add(roi);
                                }
                            }
                        }
                    }
                }
            }

            // Очистка областей с предыдущего шага
//            if (roiSize != 20) {
//                List<Rect> filteredRects = new ArrayList<>();
//                for (Rect rect : foundRects) {
//                    if (rect.width != roiSize) {
//                        filteredRects.add(rect);
//                    }
//                }
//                foundRects = filteredRects;
//            }
        }

        // Отрисовка найденых областей
        for (Rect rect : foundRects) {
            Imgproc.rectangle(image, rect, new Scalar(0, 255, 0), 2);
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
