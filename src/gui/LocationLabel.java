package gui;

import javax.swing.*;
import java.awt.*;

import java.util.ArrayList;
import java.util.List;

public class LocationLabel extends JLabel {

    public static List<Point> points = new ArrayList<>(4);

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.MAGENTA);
        g2.setStroke(new BasicStroke(3));
        for (Point point : points) {
            g2.fillOval(point.x - 3, point.y - 3, 6, 6); // Рисуем точку
        }
        g2.setColor(Color.RED);
        if (points.size() == 4) {
            // Сортируем точки
//            sortPointList();

            g2.drawLine(points.get(0).x, points.get(0).y, points.get(1).x, points.get(1).y);
            g2.drawLine(points.get(1).x, points.get(1).y, points.get(2).x, points.get(2).y);
            g2.drawLine(points.get(2).x, points.get(2).y, points.get(3).x, points.get(3).y);
            g2.drawLine(points.get(3).x, points.get(3).y, points.get(0).x, points.get(0).y);
        }
    }

    private void sortPointList() {
        points.sort((p1, p2) -> {
            if (p1.y != p2.y) {
                return Integer.compare(p1.y, p2.y);
            }
            return Integer.compare(p1.x, p2.x);
        });

        Point topRight = points.get(1).x > points.get(2).x ? points.get(2) : points.get(1);
        Point bottomRight = points.get(1).x > points.get(2).x ? points.get(1) : points.get(2);

        points.set(1, topRight);
        points.set(2, bottomRight);
    }
}
