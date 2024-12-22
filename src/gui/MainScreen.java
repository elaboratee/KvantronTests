package gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainScreen extends JFrame {

    private static final Toolkit tk = Toolkit.getDefaultToolkit();

    public static void showMainScreen() {
        // Настройка фрейма
        JFrame frame = new JFrame("Kvantron Tests");
        frame.setSize(getScreenHeight() / 2, getScreenWidth() / 3);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        // Установка иконки
        ImageIcon imageIcon = new ImageIcon(
                "media" + File.separator + "app" + File.separator + "logo.png"
        );
        frame.setIconImage(imageIcon.getImage());

        // Создание панели выделения кода
        LocateCodePanel locateCodePanel = LocateCodePanel.getInstance();

        // Создание панели вкладок
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
        tabbedPane.addTab("Выделить код на изображении", locateCodePanel);

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setVisible(true);
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
