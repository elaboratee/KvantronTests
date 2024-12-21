package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

public class LocationLabel extends JLabel {

    private List<Point> points = new ArrayList<>(4);

    public LocationLabel() {
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (points.size() < 4) {
                    points.add(new Point(e.getX(), e.getY()));
                    System.out.println("Point {" + e.getX() + ", " + e.getY() + "}");
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    public List<Point> getPoints() {
        return points;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(7.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (points != null) {
            for (Point point : points) {
                g2.drawLine(point.x, point.y, point.x, point.y);
            }
        }
    }
}
