package util;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MorphOps {

    public static Mat removeLargeBlackAreas(Mat image) {
        // Проверка корректности изображения
        if (image.channels() != 1) {
            return image;
        }

        // Ядро для удаления крупных шумовых элементов
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(13, 13));

        // Подготовка структур данных
        Mat dilatedImage = new Mat();
        Mat subtractImage = new Mat();

        // Морфологическая обработка для удаления крупных шумовых элементов
        Imgproc.dilate(image, dilatedImage, kernel);

        // Вычитание изображений для удаления крупных шумовых элементов
        Core.bitwise_not(dilatedImage, dilatedImage);
        Core.bitwise_or(image, dilatedImage, subtractImage);

        return subtractImage;
    }

    public static Mat findConnectedComponents(Mat binaryImage) {
        Mat result = Mat.zeros(binaryImage.size(), CvType.CV_8UC1);

        // Проверка корректности изображения
        if (binaryImage.channels() == 1) {
            // Инверсия бинарного изображения
            Core.bitwise_not(binaryImage, binaryImage);

            Mat visited = Mat.zeros(binaryImage.size(), CvType.CV_8UC1);
            Mat structElem = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));

            for (int y = 0; y < binaryImage.rows(); y++) {
                for (int x = 0; x < binaryImage.cols(); x++) {
                    // Если пиксель не посещен и является частью объекта
                    if (binaryImage.get(y, x)[0] == 255 && visited.get(y, x)[0] == 0) {
                        // Создание начальной маски компоненты
                        Mat componentMask = Mat.zeros(binaryImage.size(), CvType.CV_8UC1);
                        componentMask.put(y, x, 255); // Начальная точка

                        Mat prevMask = Mat.zeros(binaryImage.size(), CvType.CV_8UC1);

                        // Итеративное морфологическое расширение
                        while (true) {
                            Mat newMask = new Mat();
                            Imgproc.dilate(componentMask, newMask, structElem);

                            // Ограничние по бинарному изображению
                            Core.bitwise_and(newMask, binaryImage, newMask);

                            // Если изменений больше нет, происходит завершение построения компоненты
                            if (Core.countNonZero(newMask) == Core.countNonZero(prevMask)) {
                                break;
                            }

                            newMask.copyTo(componentMask);
                            newMask.copyTo(prevMask);
                        }

                        // Найденная компонента помечается как посещенная
                        Core.bitwise_or(visited, componentMask, visited);

                        if (Core.countNonZero(componentMask) >= 3500) {
                            Core.bitwise_or(result, componentMask, result);
                        }
                    }
                }
            }
        }

        return result;
    }
}
