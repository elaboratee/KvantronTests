package gui;

import exception.ImageReadException;
import org.opencv.core.Mat;
import util.ImageIO;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static gui.LocationLabel.points;

public class LocateCodePanel extends JPanel {

    private JPanel panel, paramPanel, imagePanel;
    private Mat image;
    private JLabel imageLabel;
    private JButton loadImageButton;
    private JButton clearPointsButton;
    private JButton recognizeBarcodeButton;
    private JLabel locationLabel;
    private boolean loadingImage = false;
    private final Toolkit tk = Toolkit.getDefaultToolkit();

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
        recognizeBarcodeButton = createButton("Распознать", e -> clearPoints());

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
        locationLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (points.size() < 4 && loadingImage) {
                    points.add(new Point(e.getX(), e.getY()));
                    System.out.println("Point {x = " + e.getX() + ", y = " + e.getY() + "}");
                    locationLabel.repaint();
                } else if (points.size() >= 4){
                    System.out.println("Штрих-код уже обведен");
                }
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
                loadingImage = true;

                // Сначала создаем imageLabel
                imageLabel = createImageLabel();
                // Отображаем изображение в imageLabel
                displayImage(image, imageLabel);

                // Теперь создаем панель для изображения и добавляем imageLabel в нее
                JPanel imagePanel = new JPanel(new BorderLayout());
                imagePanel.add(imageLabel, BorderLayout.CENTER);

                // Создаем окно для отображения изображения
                JFrame imageFrame = new JFrame("Загруженное изображение");
                imageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // Закрытие окна
                imageFrame.add(imagePanel);
                imageFrame.pack();  // Автоматически подгоняет размер окна под содержимое
                imageFrame.setVisible(true);  // Показываем окно с изображением

            } catch (ImageReadException ire) {
                showErrorDialog(ire.getMessage());
            }
        }
    }

    private void clearPoints() {
        points.clear();
        locationLabel.repaint();
        System.out.println("Точки очищены");
    }

    private void recognizeBarcode(){

    }

    private JFileChooser createImageFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите изображение");
        fileChooser.setCurrentDirectory(new File("C:\\KvantronTests\\media\\distorted_datamatrix"));
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

        panel.repaint();
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
