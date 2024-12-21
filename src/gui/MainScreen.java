package gui;

import javax.swing.*;
import java.awt.*;

public class MainScreen extends JFrame {

    private static final Toolkit tk = Toolkit.getDefaultToolkit();

    public static void showMainScreen() {
        // Настройка фрейма
        JFrame frame = new JFrame("Kvantron Tests");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(getScreenCenter());
        frame.setSize(getScreenWidth() / 2, getScreenHeight() / 2);

        // Установка иконки
        ImageIcon imageIcon = new ImageIcon("media/app/logo.png");
        frame.setIconImage(imageIcon.getImage());

        // Создание панели выделения кода
        LocateCodePanel locateCodePanel = LocateCodePanel.getInstance();

        // Создание панели вкладок
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
        tabbedPane.addTab("Выделить код на изображении", locateCodePanel.getLocateCodePanel());

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    // Метод для получения центра экрана
    private static Point getScreenCenter() {
        return new Point(
                getScreenWidth() / 2 - getScreenWidth() / 4,
                getScreenHeight() / 2 - getScreenHeight() / 4
        );
    }

    // Метод для получения ширины экрана
    private static int getScreenWidth() {
        return tk.getScreenSize().width;
    }

    // Метод для получения высоты экрана
    private static int getScreenHeight() {
        return tk.getScreenSize().height;
    }
}
