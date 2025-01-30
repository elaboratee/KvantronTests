package util;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


public class BarcodeLocalization {
    public static void localizeBarcodes(Mat binaryImage, Mat image) {

        Rect roi;
        Mat subMat;
        int roiSize = 18;
        List<Rect> barcodes = new ArrayList<>();
        int count = 0;

        // Локализация с динамическим подбором размера ядра
        for (int y = 0; y < binaryImage.rows() - roiSize; y += roiSize / 2) {
            for (int x = 0; x < binaryImage.cols() - roiSize; x += roiSize) {
                roi = new Rect(x, y, roiSize, roiSize);
                subMat = binaryImage.submat(roi);
                int transitions = countTransitions(subMat);




                if (transitions >= 30 && transitions <= 80) {

                    barcodes.add(roi);
                }
            }
        }

        barcodes = mergeRectangles(barcodes);

        seekDatamatrix(barcodes, binaryImage);

        printRectangle(barcodes, image);
    }

    public static void seekDatamatrix(List<Rect> barcodes, Mat binaryImage) {
        for (Rect barcode : barcodes) {
            Mat BinaryImageROI = new Mat(binaryImage, barcode);
            MatrixRegionBinary.findLineSupportRegions(BinaryImageROI, 50, 20);
            MatrixRegionBinary.saveGradientImage();
        }
    }





    private static double findAverageArea(List<Rect> rects){
        int count = rects.size();
        double summaryArea = 0;
        for (Rect rect : rects) {
            summaryArea += rect.width * rect.height;
        }
        return summaryArea / count;
    }


    public static void printRectangle(List<Rect> rects, Mat image) {
        for (Rect rect : rects) {
            Point topLeft = rect.tl();
            Point bottomRight = rect.br();
            Imgproc.rectangle(
                    image,
                    topLeft,
                    bottomRight,
                    new Scalar(0, 255, 0),
                    1
            );
        }
    }

    private static int countTransitions(Mat region) {
        int transitions = 0;
        for (int y = 0; y < region.rows(); y++) {
            for (int x = 0; x < region.cols() - 1; x++) {
                int currentPixel = (int) region.get(y, x)[0];
                int nextPixel = (int) region.get(y, x + 1)[0];
                if (Math.abs(currentPixel - nextPixel) > 128) { // Пороговое значение
                    transitions++;
                }
            }
        }
        return transitions;
    }

    private static List<Rect> mergeRectangles(List<Rect> rects) {
        List<Rect> mergedRects = new ArrayList<>();
        boolean[] merged = new boolean[rects.size()];

        for (int i = 0; i < rects.size(); i++) {
            if (merged[i]) continue;

            Rect base = rects.get(i);
            for (int j = i + 1; j < rects.size(); j++) {
                if (merged[j]) continue;

                Rect compare = rects.get(j);
                if (rectsOverlap(base, compare)) {
                    base = unionRects(base, compare);
                    merged[j] = true;
                }
            }
            mergedRects.add(base);
            merged[i] = true;
        }

        double averageArea = findAverageArea(mergedRects);
        for (Rect rect : mergedRects) {
            int currentArea = rect.height * rect.width;
            if (currentArea > averageArea) {

                // Добавляем по 10 пикселей ко всем сторонам прямоугольника
                rect.x -= 10;  // Сдвигаем левый верхний угол на 10 пикселей влево
                rect.y -= 10;  // Сдвигаем верхний угол на 10 пикселей вверх
                rect.width += 20;  // Увеличиваем ширину на 20 пикселей (по 10 с каждой стороны)
                rect.height += 20;  // Увеличиваем высоту на 20 пикселей (по 10 сверху и снизу)

            }
        }
        return mergedRects;
    }

    private static boolean rectsOverlap(Rect r1, Rect r2) {
        return r1.tl().x <= r2.br().x &&
                r1.br().x >= r2.tl().x &&
                r1.tl().y <= r2.br().y &&
                r1.br().y >= r2.tl().y;
    }

    private static Rect unionRects(Rect r1, Rect r2) {
        int x1 = (int) Math.min(r1.tl().x, r2.tl().x);
        int y1 = (int) Math.min(r1.tl().y, r2.tl().y);
        int x2 = (int) Math.max(r1.br().x, r2.br().x);
        int y2 = (int) Math.max(r1.br().y, r2.br().y);
        return new Rect(new Point(x1, y1), new Point(x2, y2));
    }
}



