package util;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.*;

public class MatrixRegionBinary {

    private static final int[] patternX = {-1, 0, 1, 1, 1, 0, -1, -1}; // Смещения по X
    private static final int[] patternY = {1, 1, 1, 0, -1, -1, -1, 0}; // Смещения по Y

    private static Mat gradientMagnitude;
    private static Mat gradientAngle;
    private static int countImageSave = 0;

    public static List<PointFlow> findLineSupportRegions(Mat image, double thresholdRho, double thresholdTau) {

         gradientMagnitude = new Mat(image.rows(), image.cols(), image.type());
         gradientAngle = new Mat(image.rows(), image.cols(), image.type());

         computeGradients(image, gradientMagnitude, gradientAngle);

        List<PointFlow> lineSupportRegions = new ArrayList<>();
        Set<Point> visited = new HashSet<>();
        List<PointFlow> pixels = new ArrayList<>();

        for (int y = 1; y < image.rows() - 1; y++) {
            for (int x = 1; x < image.cols() - 1; x++) {
                double magnitude = gradientMagnitude.get(y, x)[0];
                if (magnitude > thresholdRho) {
                    pixels.add(new PointFlow(x, y, -1, magnitude, -1));
                }
            }
        }

        pixels.sort((p1, p2) -> Double.compare(p2.magnitude, p1.magnitude));
        int regionId = 0;

        for (PointFlow pixel : pixels) {
            if (!visited.contains(new Point(pixel.x, pixel.y))) {
                Queue<PointFlow> queue = new LinkedList<>();
                queue.add(pixel);
                pixel.regionId = regionId;
                visited.add(new Point(pixel.x, pixel.y));

                while (!queue.isEmpty()) {
                    PointFlow current = queue.poll();
                    for (PointFlow neighbor : getNeighbors(current, gradientMagnitude, gradientAngle, thresholdTau, visited, regionId)) {
                        neighbor.regionId = regionId;
                        queue.add(neighbor);
                        visited.add(new Point(neighbor.x, neighbor.y));
                        lineSupportRegions.add(neighbor);
                    }
                }
                regionId++;
            }
        }
        return lineSupportRegions;
    }



    private static void computeGradients(Mat image, Mat gradientMagnitude, Mat gradientAngle) {
        for (int y = 1; y < image.rows() - 1; y++) {
            for (int x = 1; x < image.cols() - 1; x++) {
                double gx = (image.get(y, x + 1)[0] - image.get(y, x - 1)[0]) / 2.0;
                double gy = (image.get(y + 1, x)[0] - image.get(y - 1, x)[0]) / 2.0;

                // Модуль градиента (сила)
                double magnitude = Math.sqrt(gx * gx + gy * gy);

                // Угол градиента (в градусах, от -180 до 180)
                double angle = Math.toDegrees(Math.atan2(gy, gx));

                gradientMagnitude.put(y, x, magnitude);
                gradientAngle.put(y, x, angle);
            }
        }
    }

    private static List<PointFlow> getNeighbors(PointFlow center,
                                                Mat gradientMagnitude,
                                                Mat gradientAngle,
                                                double thresholdTau,
                                                Set<Point> visited,
                                                int regionId) {

        List<PointFlow> neighbors = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            int neighborX = center.x + patternX[i];
            int neighborY = center.y + patternY[i];

            if (neighborX >= 0 && neighborY >= 0 && neighborX < gradientMagnitude.cols() && neighborY < gradientMagnitude.rows() &&
                    !visited.contains(new Point(neighborX, neighborY))) {
                double neighborAngle = gradientAngle.get(neighborY, neighborX)[0];

                if (Math.abs(neighborAngle - calculateRegionAngle(neighbors)) < thresholdTau) {
                    neighbors.add(new PointFlow(neighborX, neighborY, -1, gradientMagnitude.get(neighborY, neighborX)[0], regionId));
                }
            }
        }
        return neighbors;
    }

    private static double calculateRegionAngle(List<PointFlow> region) {

        double sumSin = 0, sumCos = 0;

        for (PointFlow pixel : region) {
            double angle = Math.toRadians(pixel.magnitude);
            sumSin += Math.sin(angle);
            sumCos += Math.cos(angle);
        }

        return Math.toDegrees(Math.atan2(sumSin, sumCos));
    }

    public static void saveGradientImage() {

        Mat normalizedMagnitude = new Mat();
        Core.normalize(gradientAngle, normalizedMagnitude, 0, 255, Core.NORM_MINMAX);
        normalizedMagnitude.convertTo(normalizedMagnitude, CvType.CV_8U);
        Imgcodecs.imwrite(countImageSave + "gradient_magnitude.png", normalizedMagnitude);

        countImageSave += 1;
    }

}



