package gui;

import javax.swing.*;
import java.awt.*;

public class LocateCodePanel extends JPanel {

    private JPanel panel, paramPanel, imagePanel;

    private LocateCodePanel() {}

    public static LocateCodePanel getInstance() {
        return new LocateCodePanel();
    }

    public JPanel getLocateCodePanel() {
        panel = new JPanel(new GridLayout(2, 1, 10, 10));

        paramPanel = new JPanel();
        imagePanel = new JPanel();

        panel.add(paramPanel);
        panel.add(imagePanel);

        return panel;
    }
}
