package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

public class LocationLabel extends JLabel{

    private List<Point> points = new ArrayList<>(4);



    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.RED);
        if (points != null) {
            for (Point point : points) {
                g2.drawRect(point.x, point.y, 1, 1);
            }
        }
    }
}
