package gui;

import exception.ImageReadException;
import org.opencv.core.Mat;
import util.BarcodeProcessing;
import util.DataConversion;
import util.ImageIO;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static util.ImagePoints.*;


public class LocateCodePanel extends JPanel {

    private JPanel panel, paramPanel, imagePanel;
    private Mat image;
    private JLabel imageLabel;
    private JButton loadImageButton;
    private JButton clearPointsButton;
    private JButton recognizeBarcodeButton;
    private JLabel locationLabel;

    private LocateCodePanel() {
    }

    public static LocateCodePanel getInstance() {
        return new LocateCodePanel();
    }

    public JPanel getLocateCodePanel() {
        panel = new JPanel();

        paramPanel = createButtonPanel();
        panel.add(paramPanel);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 0, 0));
        panel.setOpaque(false);

        loadImageButton = createButton("Загрузить изображение", e -> loadImage());
        clearPointsButton = createButton("Очистить точки", e -> clearPoints());
        recognizeBarcodeButton = createButton("Распознать", e -> recognizeBarcode());

        clearPointsButton.setEnabled(false);
        recognizeBarcodeButton.setEnabled(false);

        panel.add(loadImageButton);
        panel.add(clearPointsButton);
        panel.add(recognizeBarcodeButton);

        return panel;
    }

    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setPreferredSize(new Dimension(200, 100));
        return button;
    }

    private JLabel createImageLabel() {
        locationLabel= new LocationLabel();
        locationLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (points.size() < 4 && image != null) {
                    points.add(new Point(e.getX(), e.getY()));
                    System.out.println("Point {x = " + e.getX() + ", y = " + e.getY() + "}");
                    if (points.size() == 4) {
                        recognizeBarcodeButton.setEnabled(true);
                    }
                    locationLabel.repaint();
                } else {
                    System.out.println("Штрих-код уже обведен");
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
        JFileChooser fileChooser = createImageFileChooser();
        if (fileChooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
            String imagePath = fileChooser.getSelectedFile().getAbsolutePath();
            try {
                image = ImageIO.loadImage(imagePath);

                // Очищаем точки после загрузки
                clearPoints();

                // Отключаем кнопку загрузки
                loadImageButton.setEnabled(false);


                // Создание JLabel для изображения
                imageLabel = createImageLabel();

                // Отображение изображения
                displayImage(image, imageLabel);

                // Создание панели для изображения
                JPanel imagePanel = new JPanel(new BorderLayout());
                imagePanel.add(imageLabel, BorderLayout.CENTER);

                // Создание фрейма для изображения
                JFrame imageFrame = createImageFrame("Загруженное изображение");

                imageFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        super.windowClosed(e);
                        loadImageButton.setEnabled(true);
                        recognizeBarcodeButton.setEnabled(true);
                    }
                });

                imageFrame.add(imagePanel);
                imageFrame.pack();
                imageFrame.setVisible(true);
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
            System.out.println("Точки очищены");
        } else {
            System.out.println("Точек нет");
        }
    }

    private void recognizeBarcode() {
        BufferedImage barcodeBitmap = BarcodeProcessing.processBarcode(DataConversion.matToBufferedImage(image),
                                                                        minX, minY, width, height);
        JLabel bitmapLabel = new JLabel();

        displayBitmapImage(barcodeBitmap, bitmapLabel);
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.add(bitmapLabel, BorderLayout.CENTER);

        JFrame imageFrame = createImageFrame("Bitmap barcode");

        imageFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                recognizeBarcodeButton.setEnabled(true);
            }
        });

        imageFrame.add(imagePanel);
        imageFrame.pack();
        imageFrame.setVisible(true);

        recognizeBarcodeButton.setEnabled(false);
    }

    private JFileChooser createImageFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите изображение");
        fileChooser.setCurrentDirectory(new File("media" + File.separator + "distorted_datamatrix"));
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Изображения (JPG, PNG, BMP)", "jpg", "jpeg", "png", "bmp")
        );
        return fileChooser;
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(panel, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void displayImage(Mat image, JLabel label) {
        // Получение оригинальных размеров изображения
        BufferedImage bufferedImage = DataConversion.matToBufferedImage(image);

        // Установка изображения на JLabel
        ImageIcon imageIcon = new ImageIcon(bufferedImage);
        label.setIcon(imageIcon);
    }

    private void displayBitmapImage(BufferedImage image, JLabel label) {

        // Установка изображения на JLabel
        ImageIcon imageIcon = new ImageIcon(image);
        label.setIcon(imageIcon);
    }
}
