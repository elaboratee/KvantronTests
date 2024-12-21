package gui;

import exception.ImageReadException;
import org.opencv.core.Mat;
import util.ImageIO;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static gui.LocationLabel.points;

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
                    locationLabel.repaint();
                } else if (points.size() >= 4){
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
                JFrame imageFrame = createImageFrame();
                imageFrame.add(imagePanel);
                imageFrame.pack();
                imageFrame.setVisible(true);
            } catch (ImageReadException ire) {
                showErrorDialog(ire.getMessage());
            }
        }
    }

    private JFrame createImageFrame() {
        // Создание окна для отображения изображения
        JFrame imageFrame = new JFrame("Загруженное изображение");
        imageFrame.setResizable(false);
        imageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        imageFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                loadImageButton.setEnabled(true);
            }
        });

        return imageFrame;
    }

    private void clearPoints() {
        if (!points.isEmpty()) {
            points.clear();
            clearPointsButton.setEnabled(false);
            locationLabel.repaint();
            System.out.println("Точки очищены");
        } else {
            System.out.println("Точек нет");
        }
    }

    private void recognizeBarcode() {

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
        BufferedImage bufferedImage = matToBufferedImage(image);

        // Установка изображения на JLabel
        ImageIcon imageIcon = new ImageIcon(bufferedImage);
        label.setIcon(imageIcon);
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type;
        if (mat.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (mat.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        } else {
            throw new IllegalArgumentException("Не поддерживаемое количество каналов матрицы: " + mat.channels());
        }

        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();
        byte[] data = new byte[width * height * channels];

        mat.get(0, 0, data);
        BufferedImage bufferedImage = new BufferedImage(width, height, type);
        bufferedImage.getRaster().setDataElements(0, 0, width, height, data);
        return bufferedImage;
    }
}
