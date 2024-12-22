package gui;

import exception.ImageReadException;
import org.opencv.core.Mat;
import util.BarcodeProcessing;
import util.DataConversions;
import util.ImageIO;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static util.ImagePoints.*;


public class LocateCodePanel extends JPanel {

    private static LocateCodePanel instance;

    private final JPanel buttonPanel, imagePanel, logPanel;
    private final JLabel locationLabel;
    private JButton loadImageButton, clearPointsButton, recognizeBarcodeButton;
    private final JFileChooser fileChooser;
    private JTextArea actionLog;
    private Mat image;

    private LocateCodePanel() {
        // Создание панели кнопок
        buttonPanel = createButtonPanel();

        // Создание панели логов
        logPanel = createLogPanel();

        // Заполнение родительской панели
        setLayout(new BorderLayout());
        add(buttonPanel, BorderLayout.NORTH);
        add(logPanel, BorderLayout.CENTER);

        // Создание панели изображения
        imagePanel = createImagePanel();

        // Создание лейбла изображения
        locationLabel = createLocationLabel();
        imagePanel.add(locationLabel, BorderLayout.CENTER);

        // Создание окна для выбора файлов
        fileChooser = createImageFileChooser();
    }

    public static LocateCodePanel getInstance() {
        if (instance == null) {
            instance = new LocateCodePanel();
        }
        return instance;
    }

    public static JPanel getLogPanel() {
        return getInstance().logPanel;
    }

    public static JTextArea getActionLog() {
        return getInstance().actionLog;
    }

    private JPanel createButtonPanel() {
        // Создание панели кнопок
        JPanel panel = new JPanel(new GridLayout(2, 2, 0, 0));
        panel.setOpaque(false);

        // Создание кнопок и привязка обработчиков событий
        loadImageButton = createButton("Загрузить изображение", e -> loadImage());
        clearPointsButton = createButton("Очистить точки", e -> clearPoints());
        recognizeBarcodeButton = createButton("Распознать", e -> recognizeBarcode());

        // Выключение кнопок
        clearPointsButton.setEnabled(false);
        recognizeBarcodeButton.setEnabled(false);

        // Добавление кнопок на панель
        panel.add(loadImageButton);
        panel.add(clearPointsButton);
        panel.add(recognizeBarcodeButton);

        return panel;
    }

    private JPanel createLogPanel() {
        // Создание панели для логов
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // Создание текстовой области для логов
        actionLog = createActionLog();
        panel.add(new JScrollPane(actionLog), BorderLayout.CENTER);

        return panel;
    }

    private JTextArea createActionLog() {
        JTextArea textArea = new JTextArea(5, 20);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    private JPanel createImagePanel() {
        return new JPanel(new BorderLayout());
    }

    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setPreferredSize(new Dimension(200, 100));
        return button;
    }

    private JLabel createLocationLabel() {
        JLabel locationLabel = new LocationLabel();
        locationLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (points.size() < 4 && image != null) {
                    points.add(new Point(e.getX(), e.getY()));
                    if (points.size() == 4) {
                        recognizeBarcodeButton.setEnabled(true);
                    }
                    locationLabel.repaint();
                } else {
                    logAction("Штрих-код уже выделен");
                }

                // Включаем кнопку очистки точек
                if (points.size() == 1) {
                    clearPointsButton.setEnabled(true);
                }
            }
        });
        locationLabel.setHorizontalAlignment(SwingConstants.CENTER);
        locationLabel.setVerticalAlignment(SwingConstants.CENTER);

        return locationLabel;
    }

    private void loadImage() {
        if (fileChooser.showOpenDialog(buttonPanel) == JFileChooser.APPROVE_OPTION) {
            String imagePath = fileChooser.getSelectedFile().getAbsolutePath();
            try {
                // Загрузка изображения
                image = ImageIO.loadImage(imagePath);
                logAction("\nЗагружено изображение: " + fileChooser.getSelectedFile().getName());

                // Очистка точек после загрузки
                clearPoints();

                // Отключение кнопки загрузки
                loadImageButton.setEnabled(false);

                // Отображение изображения
                displayImage(image, locationLabel);

                // Создание фрейма для изображения
                JFrame imageFrame = createImageFrame("Loaded Image");

                imageFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        super.windowClosed(e);
                        clearPoints();
                        loadImageButton.setEnabled(true);
                        clearPointsButton.setEnabled(false);
                        recognizeBarcodeButton.setEnabled(false);
                    }
                });
                imageFrame.add(imagePanel);
                imageFrame.pack();
                imageFrame.setVisible(true);
                imageFrame.setLocationRelativeTo(null);
            } catch (ImageReadException ire) {
                showErrorDialog(ire.getMessage());
            }
        }
    }

    private JFrame createImageFrame(String title) {
        // Создание окна для отображения изображения
        JFrame imageFrame = new JFrame(title);
        imageFrame.setResizable(false);
        imageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        return imageFrame;
    }

    private void clearPoints() {
        if (!points.isEmpty()) {
            points.clear();
            clearPointsButton.setEnabled(false);
            recognizeBarcodeButton.setEnabled(false);
            locationLabel.repaint();
            logAction("Точки очищены");
        } else {
            logAction("Точек нет");
        }
    }

    private void recognizeBarcode() {
        recognizeBarcodeButton.setEnabled(false);

        BufferedImage barcodeBitmap = BarcodeProcessing.processBarcode(
                DataConversions.matToBufferedImage(image),
                minX, minY,
                width, height
        );

        JLabel bitmapLabel = new JLabel();
        displayImage(barcodeBitmap, bitmapLabel);

        JPanel imagePanel = createImagePanel();
        imagePanel.add(bitmapLabel, BorderLayout.CENTER);

        JFrame imageFrame = createImageFrame("Barcode Bitmap");

        imageFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                if (!points.isEmpty()) {
                    recognizeBarcodeButton.setEnabled(true);
                }
            }
        });
        imageFrame.add(imagePanel);

        imageFrame.pack();
        imageFrame.setVisible(true);
        imageFrame.setLocationRelativeTo(null);
    }

    private JFileChooser createImageFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите изображение");
        fileChooser.setCurrentDirectory(new File("media" + File.separator + "codes"));
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Изображения (JPG, PNG, BMP)", "jpg", "jpeg", "png", "bmp")
        );
        return fileChooser;
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(buttonPanel, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void displayImage(Mat image, JLabel label) {
        // Получение оригинальных размеров изображения
        BufferedImage bufferedImage = DataConversions.matToBufferedImage(image);

        // Установка изображения на JLabel
        ImageIcon imageIcon = new ImageIcon(bufferedImage);
        label.setIcon(imageIcon);
    }

    private void displayImage(BufferedImage image, JLabel label) {
        // Установка изображения на JLabel
        ImageIcon imageIcon = new ImageIcon(image);
        label.setIcon(imageIcon);
    }

    private void logAction(String action) {
        actionLog.append(action + "\n");
        actionLog.setCaretPosition(actionLog.getDocument().getLength());
    }
}
