package distorted_datamatrix;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Main {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // Загрузка изображения
        Mat img = Imgcodecs.imread(
                "media/distorted_datamatrix/og-traceway-datamatrix.jpg",
                Imgcodecs.IMREAD_COLOR
        );
        if (img.empty()) {
            System.err.println("Не удалось загрузить изображение!");
            return;
        }

        // Преобразование к оттенкам серого
        Mat imgGray = new Mat();
        Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite("media/distorted_datamatrix/test/test_gray.jpg", imgGray);

        // Выделение контуров
        Mat edges = new Mat();
        Imgproc.Canny(imgGray, edges, 100, 500);
        Imgcodecs.imwrite("media/distorted_datamatrix/test/test_edges.jpg", edges);

        // Поиск контуров
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // Поиск контура, схожего с Data Matrix
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        for (MatOfPoint contour : contours) {
            // Аппроксимация контура многоугольником
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            double epsilon = 0.02 * Imgproc.arcLength(contour2f, true);
            Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true);

            // Проверяем, является ли контур четырехугольником
            if (approxCurve.total() == 4) {
                Point[] points = approxCurve.toArray();

                // Упорядочиваем углы для перспективной трансформации
                Point[] sortedPoints = sortPoints(points);

                // Исходные и целевые точки для трансформации
                MatOfPoint2f srcPoints = new MatOfPoint2f(sortedPoints);
                MatOfPoint2f dstPoints = new MatOfPoint2f(
                        new Point(0, 0),
                        new Point(300, 0),
                        new Point(300, 300),
                        new Point(0, 300)
                );

                // Вычисление матрицы трансформации
                Mat transformMat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);

                // Применение перспективной трансформации
                Mat correctedImg = new Mat();
                Imgproc.warpPerspective(img, correctedImg, transformMat, new Size(300, 300));

                // Сохранение результата
                Imgcodecs.imwrite("media/distorted_datamatrix/test/test_trans.jpg", correctedImg);
            }
        }
    }

    /* Метод для сортировки углов четырехугольника в порядке:
     * верхний левый, верхний правый, нижний правый, нижний левый
     */
    private static Point[] sortPoints(Point[] points) {
        List<Point> sorted = new ArrayList<>(List.of(points));
        sorted.sort((p1, p2) -> {
            if (p1.y != p2.y) return Double.compare(p1.y, p2.y); // Сначала сортируем по Y
            return Double.compare(p1.x, p2.x); // Затем сортируем по X
        });

        Point topLeft = sorted.get(0);
        Point topRight = sorted.get(1).x > sorted.get(2).x ? sorted.get(2) : sorted.get(1);
        Point bottomRight = sorted.get(1).x > sorted.get(2).x ? sorted.get(1) : sorted.get(2);
        Point bottomLeft = sorted.get(3);

        return new Point[]{topLeft, topRight, bottomRight, bottomLeft};
    }
}
