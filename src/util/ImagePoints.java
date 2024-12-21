package util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ImagePoints {

    public static List<Point> points = new ArrayList<>(4);
    public static int width, height;
    public static int maxX, maxY;
    public static int minX, minY;

    // Необходимо для рисования (не для вычисления координат)
    public static void sortPoints() {
        // Вычисляем центр всех точек
        double centerX = points.stream().mapToDouble(p -> p.x).average().orElse(0);
        double centerY = points.stream().mapToDouble(p -> p.y).average().orElse(0);

        // Сортируем точки по углу относительно центра
        points.sort((p1, p2) -> {
            double angle1 = Math.atan2(p1.y - centerY, p1.x - centerX);
            double angle2 = Math.atan2(p2.y - centerY, p2.x - centerX);
            return Double.compare(angle1, angle2);
        });
    }

    public static void findBarcodeBorders() {
        maxX = maxY = Integer.MIN_VALUE;
        minX = minY = Integer.MAX_VALUE;
        for (Point point : points) {
            if (point.getX() > maxX) maxX = (int) point.getX();
            if (point.getY() > maxY) maxY = (int) point.getY();
            if (point.getX() < minX) minX = (int) point.getX();
            if (point.getY() < minY) minY = (int) point.getY();
        }
        System.out.println("maxX: " + maxX + "\nminX:" + minX + "\nmaxY: " + maxY + "\nminY:" + minY);
        width = maxX - minX;
        height = maxY - minY;
        System.out.println("\n\nwidth: " + width + "\nheight: " + height);
    }
}
