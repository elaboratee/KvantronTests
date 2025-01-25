package gui;

import org.opencv.core.Point;
import util.ImagePoints;

import javax.swing.*;
import java.awt.*;

import static util.ImagePoints.points;

public class LocationLabel extends JLabel {

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;

        // Настройка кисти
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(3));

        // Отрисовка точек
        for (Point point : points) {
            g2.fillOval((int) point.x - 3, (int) point.y - 3, 6, 6); // Рисуем точку
        }

        if (points.size() == 4) {
            // Сортировка точек
            ImagePoints.sortPoints();

            // Поиск границ штрих-кода
            ImagePoints.findBarcodeBorders();

            // Отрисовка линий
            g2.drawLine((int) points.get(0).x, (int) points.get(0).y, (int) points.get(1).x, (int) points.get(1).y);
            g2.drawLine((int) points.get(1).x, (int) points.get(1).y, (int) points.get(2).x, (int) points.get(2).y);
            g2.drawLine((int) points.get(2).x, (int) points.get(2).y, (int) points.get(3).x, (int) points.get(3).y);
            g2.drawLine((int) points.get(3).x, (int) points.get(3).y, (int) points.get(0).x, (int) points.get(0).y);
        }
    }
}
