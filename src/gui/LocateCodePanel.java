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
import java.util.ArrayList;
import java.util.List;

public class LocateCodePanel extends JPanel {

    private JPanel panel, paramPanel, imagePanel;
    private Mat image;
    private LocationLabel imageLabel;
    private JButton loadImageButton, cropImageButton;
    private final Toolkit tk = Toolkit.getDefaultToolkit();

    private LocateCodePanel() {
    }

    public static LocateCodePanel getInstance() {
        return new LocateCodePanel();
    }

    public JPanel getLocateCodePanel() {
        panel = new JPanel(new GridLayout(1, 2, 10, 10));

        paramPanel = createButtonPanel();
        imagePanel = createImagePanel();

        panel.add(paramPanel);
        panel.add(imagePanel);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.setOpaque(false);

        loadImageButton = createButton("Загрузить изображение", e -> loadImage());
        cropImageButton = createButton("Вырезать код", e -> cropImage());

        panel.add(loadImageButton);
        panel.add(cropImageButton);

        return panel;
    }

    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setPreferredSize(new Dimension(75, 20));
        return button;
    }

    private JPanel createImagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0x181818));
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        LocationLabel label = createImageLabel();

        JScrollPane scrollPane = new JScrollPane(label);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(0x181818));
        panel.add(scrollPane, BorderLayout.CENTER);

        imageLabel = label;

        return panel;
    }

    private LocationLabel createImageLabel() {
        LocationLabel label = new LocationLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private void loadImage() {
        JFileChooser fileChooser = createImageFileChooser();
        if (fileChooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
            String imagePath = fileChooser.getSelectedFile().getAbsolutePath();
            try {
                image = ImageIO.loadImage(imagePath);
                displayImage(image, imageLabel);
            } catch (ImageReadException ire) {
                showErrorDialog(ire.getMessage());
            }
        }
    }

    private void cropImage() {
        if (imageLabel.getPoints().size() < 4) {
            showErrorDialog("Недостаточно точек: " + imageLabel.getPoints().size() + " вместо 4");
            return;
        }

        BufferedImage bufferedImage = matToBufferedImage(image);

        Point upperLeft = imageLabel.getPoints().get(0);
        Point upperRight = imageLabel.getPoints().get(1);
        Point downRight = imageLabel.getPoints().get(2);
        Point downLeft = imageLabel.getPoints().get(3);
    }

    private JFileChooser createImageFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите изображение");
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
        int originalWidth = bufferedImage.getWidth();
        int originalHeight = bufferedImage.getHeight();

        // Получение доступного размера панели
        int maxWidth = (int) (tk.getScreenSize().width / 2.5);
        int maxHeight = (int) (tk.getScreenSize().height / 2.0);

        // Расчет новых размеров с сохранением пропорций
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double scale = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        // Масштабирование изображения с сохранением пропорций
        Image scaledImage = bufferedImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        // Установка изображения на JLabel
        ImageIcon imageIcon = new ImageIcon(scaledImage);
        label.setIcon(imageIcon);

        panel.repaint();
    }

    private void displayImage(BufferedImage image, JLabel label) {
        // Получение доступного размера панели
        int maxWidth = (int) (tk.getScreenSize().width / 2.5);
        int maxHeight = (int) (tk.getScreenSize().height / 2.0);

        // Расчет новых размеров с сохранением пропорций
        double widthRatio = (double) maxWidth / image.getWidth();
        double heightRatio = (double) maxHeight / image.getHeight();
        double scale = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (image.getWidth() * scale);
        int newHeight = (int) (image.getHeight() * scale);

        // Масштабирование изображения с сохранением пропорций
        Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        // Установка изображения на JLabel
        ImageIcon imageIcon = new ImageIcon(scaledImage);
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
