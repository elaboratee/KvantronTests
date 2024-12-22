package gui;

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
        g2.setColor(new Color(0x00FFFF));
        g2.setStroke(new BasicStroke(3));

        // Отрисовка точек
        for (Point point : points) {
            g2.fillOval(point.x - 3, point.y - 3, 6, 6); // Рисуем точку
        }

        if (points.size() == 4) {
            // Сортировка точек
            ImagePoints.sortPoints();

            // Поиск границ штрих-кода
            ImagePoints.findBarcodeBorders();

            // Отрисовка линий
            g2.drawLine(points.get(0).x, points.get(0).y, points.get(1).x, points.get(1).y);
            g2.drawLine(points.get(1).x, points.get(1).y, points.get(2).x, points.get(2).y);
            g2.drawLine(points.get(2).x, points.get(2).y, points.get(3).x, points.get(3).y);
            g2.drawLine(points.get(3).x, points.get(3).y, points.get(0).x, points.get(0).y);
            for (Point point : points) {
                System.out.println(point);
            }
        }
    }
}
