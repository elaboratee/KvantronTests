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
        int count;

        // Локализация с динамическим подбором размера ядра
        for (int y = 0; y < binaryImage.rows() - roiSize; y += roiSize / 2) {
            for (int x = 0; x < binaryImage.cols() - roiSize; x += roiSize) {
                roi = new Rect(x, y, roiSize, roiSize);
                subMat = binaryImage.submat(roi);
                count = countTransitions(subMat);

                if (count >= 50 && count < 150) {
                    subMat.setTo(new Scalar(255));
                    barcodes.add(roi);
                }
            }
        }
        barcodes = mergeRectangles(barcodes);
        printRectangle(barcodes, image);

    }

    public static void printRectangle(List<Rect> rects, Mat image) {
        int count = rects.size();
        double summaryArea = 0;
        for (Rect rect : rects) {
            summaryArea += rect.width * rect.height;
        }
        double averageArea = summaryArea / count;

        for (Rect rect : rects) {
            int currentArea = rect.height * rect.width;
            if (currentArea > averageArea) {
                Point topLeft = rect.tl();
                Point bottomRight = rect.br();
                topLeft = new Point(topLeft.x - 5, topLeft.y - 5);
                bottomRight = new Point(bottomRight.x + 5, bottomRight.y + 5);
                Imgproc.rectangle(
                        image,
                        topLeft,
                        bottomRight,
                        new Scalar(0, 255, 0),
                        1
                );
            }
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

