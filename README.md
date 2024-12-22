# Приложение для распознавания штрихкодов

## Результат выполнения:


https://github.com/user-attachments/assets/1525f5e3-2144-4c75-a50f-cc7b888c33e4

# Передача координат штрихкода, определенных в результате предварительной локализации
### Класс ```ImagePoints```:
```java
public class ImagePoints {

    public static List<Point> points = new ArrayList<>(4);
    public static int width, height;
    public static int maxX, maxY;
    public static int minX, minY;

    // Необходимо для рисования (не для вычисления координат)
    public static void sortPoints() {
        // Вычисляем центр всех точек
        double centerX = points.stream().mapToDouble(p -> p.x).average().orElse(0);
        double centerY = points.stream().mapToDouble(p -> p.y).average().orElse(0);

        // Сортируем точки по углу относительно центра
        points.sort((p1, p2) -> {
            double angle1 = Math.atan2(p1.y - centerY, p1.x - centerX);
            double angle2 = Math.atan2(p2.y - centerY, p2.x - centerX);
            return Double.compare(angle1, angle2);
        });
    }

    public static void findBarcodeBorders() {
        maxX = maxY = Integer.MIN_VALUE;
        minX = minY = Integer.MAX_VALUE;
        for (Point point : points) {
            if (point.getX() > maxX) maxX = (int) point.getX();
            if (point.getY() > maxY) maxY = (int) point.getY();
            if (point.getX() < minX) minX = (int) point.getX();
            if (point.getY() < minY) minY = (int) point.getY();
        }
        width = maxX - minX;
        height = maxY - minY;
    }
}

```
### В методе ```findBarcodeBorders``` описано определение минимальных и максимальных координат среди заданных точек (в данном случае они заданы вручную), а также вычисление ширины и высоты области, содержащей штрихкод.
### Класс BarcodeProcessing:
```java
public class BarcodeProcessing {

    public static BufferedImage processBarcode(BufferedImage image,
                                               int minX, int minY,
                                               int width, int height) {

        LuminanceSource source = new BufferedImageLuminanceSource(image, minX, minY, width, height);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = new MultiFormatReader().decode(bitmap);
            LocateCodePanel.getActionLog().append("Текст штрих-кода: " + result.getText() + "\n");
        } catch (NotFoundException e) {
            LocateCodePanel.getActionLog().append("Штрих-код не найден\n");
        }

        return DataConversions.binaryBitmapToBufferedImage(bitmap);
    }
}
```
Данный класс был переписан из класса ```BarcodeDetection```: https://github.com/kirilllapi/Kvantron_Codebar/blob/main/LibZxing/README_ZXING.md
## Вычисленные параметры: ```image, minX, minY, width, height``` передаются напрямую в конструктор. То есть изображение вырезается по данным координатам.
```java 
LuminanceSource source = new BufferedImageLuminanceSource(image, minX, minY, width, height);
```
### Класс ```BufferedImageLuminanceSource```
```java
public final class BufferedImageLuminanceSource extends LuminanceSource {

  private static final double MINUS_45_IN_RADIANS = -0.7853981633974483; // Math.toRadians(-45.0)

  private final BufferedImage image;
  private final int left;
  private final int top;

  public BufferedImageLuminanceSource(BufferedImage image) {
    this(image, 0, 0, image.getWidth(), image.getHeight());
  }

  public BufferedImageLuminanceSource(BufferedImage image, int left, int top, int width, int height) {
    super(width, height);

    if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
      this.image = image;
    } else {
      int sourceWidth = image.getWidth();
      int sourceHeight = image.getHeight();
      if (left + width > sourceWidth || top + height > sourceHeight) {
        throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
      }

      this.image = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_BYTE_GRAY);

      WritableRaster raster = this.image.getRaster();
      int[] buffer = new int[width];
      for (int y = top; y < top + height; y++) {
        image.getRGB(left, y, width, 1, buffer, 0, sourceWidth);
        for (int x = 0; x < width; x++) {
          int pixel = buffer[x];

          // The color of fully-transparent pixels is irrelevant. They are often, technically, fully-transparent
          // black (0 alpha, and then 0 RGB). They are often used, of course as the "white" area in a
          // barcode image. Force any such pixel to be white:
          if ((pixel & 0xFF000000) == 0) {
            // white, so we know its luminance is 255
            buffer[x] = 0xFF;
          } else {
            // .299R + 0.587G + 0.114B (YUV/YIQ for PAL and NTSC),
            // (306*R) >> 10 is approximately equal to R*0.299, and so on.
            // 0x200 >> 10 is 0.5, it implements rounding.
            buffer[x] =
              (306 * ((pixel >> 16) & 0xFF) +
                601 * ((pixel >> 8) & 0xFF) +
                117 * (pixel & 0xFF) +
                0x200) >> 10;
          }
        }
        raster.setPixels(left, y, width, 1, buffer);
      }

    }
    this.left = left;
    this.top = top;
  }

  @Override
  public byte[] getRow(int y, byte[] row) {
    if (y < 0 || y >= getHeight()) {
      throw new IllegalArgumentException("Requested row is outside the image: " + y);
    }
    int width = getWidth();
    if (row == null || row.length < width) {
      row = new byte[width];
    }
    // The underlying raster of image consists of bytes with the luminance values
    image.getRaster().getDataElements(left, top + y, width, 1, row);
    return row;
  }

  @Override
  public byte[] getMatrix() {
    int width = getWidth();
    int height = getHeight();
    int area = width * height;
    byte[] matrix = new byte[area];
    // The underlying raster of image consists of area bytes with the luminance values
    image.getRaster().getDataElements(left, top, width, height, matrix);
    return matrix;
  }

  @Override
  public boolean isCropSupported() {
    return true;
  }

  @Override
  public LuminanceSource crop(int left, int top, int width, int height) {
    return new BufferedImageLuminanceSource(image, this.left + left, this.top + top, width, height);
  }

  /**
   * This is always true, since the image is a gray-scale image.
   *
   * @return true
   */
  @Override
  public boolean isRotateSupported() {
    return true;
  }

  @Override
  public LuminanceSource rotateCounterClockwise() {
    int sourceWidth = image.getWidth();
    int sourceHeight = image.getHeight();

    // Rotate 90 degrees counterclockwise.
    AffineTransform transform = new AffineTransform(0.0, -1.0, 1.0, 0.0, 0.0, sourceWidth);

    // Note width/height are flipped since we are rotating 90 degrees.
    BufferedImage rotatedImage = new BufferedImage(sourceHeight, sourceWidth, BufferedImage.TYPE_BYTE_GRAY);

    // Draw the original image into rotated, via transformation
    Graphics2D g = rotatedImage.createGraphics();
    g.drawImage(image, transform, null);
    g.dispose();

    // Maintain the cropped region, but rotate it too.
    int width = getWidth();
    return new BufferedImageLuminanceSource(rotatedImage, top, sourceWidth - (left + width), getHeight(), width);
  }

  @Override
  public LuminanceSource rotateCounterClockwise45() {
    int width = getWidth();
    int height = getHeight();

    int oldCenterX = left + width / 2;
    int oldCenterY = top + height / 2;

    // Rotate 45 degrees counterclockwise.
    AffineTransform transform = AffineTransform.getRotateInstance(MINUS_45_IN_RADIANS, oldCenterX, oldCenterY);

    int sourceDimension = Math.max(image.getWidth(), image.getHeight());
    BufferedImage rotatedImage = new BufferedImage(sourceDimension, sourceDimension, BufferedImage.TYPE_BYTE_GRAY);

    // Draw the original image into rotated, via transformation
    Graphics2D g = rotatedImage.createGraphics();
    g.drawImage(image, transform, null);
    g.dispose();

    int halfDimension = Math.max(width, height) / 2;
    int newLeft = Math.max(0, oldCenterX - halfDimension);
    int newTop = Math.max(0, oldCenterY - halfDimension);
    int newRight = Math.min(sourceDimension - 1, oldCenterX + halfDimension);
    int newBottom = Math.min(sourceDimension - 1, oldCenterY + halfDimension);

    return new BufferedImageLuminanceSource(rotatedImage, newLeft, newTop, newRight - newLeft, newBottom - newTop);
  }

}
```
### Конструктор ```BufferedImageLuminanceSource```:
```java
public BufferedImageLuminanceSource(BufferedImage image, int left, int top, int width, int height) {
    super(width, height);

    if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
      this.image = image;
    } else {
      int sourceWidth = image.getWidth();
      int sourceHeight = image.getHeight();
      if (left + width > sourceWidth || top + height > sourceHeight) {
        throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
      }

      this.image = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_BYTE_GRAY);

      WritableRaster raster = this.image.getRaster();
      int[] buffer = new int[width];
      for (int y = top; y < top + height; y++) {
        image.getRGB(left, y, width, 1, buffer, 0, sourceWidth);
        for (int x = 0; x < width; x++) {
          int pixel = buffer[x];

          // The color of fully-transparent pixels is irrelevant. They are often, technically, fully-transparent
          // black (0 alpha, and then 0 RGB). They are often used, of course as the "white" area in a
          // barcode image. Force any such pixel to be white:
          if ((pixel & 0xFF000000) == 0) {
            // white, so we know its luminance is 255
            buffer[x] = 0xFF;
          } else {
            // .299R + 0.587G + 0.114B (YUV/YIQ for PAL and NTSC),
            // (306*R) >> 10 is approximately equal to R*0.299, and so on.
            // 0x200 >> 10 is 0.5, it implements rounding.
            buffer[x] =
              (306 * ((pixel >> 16) & 0xFF) +
                601 * ((pixel >> 8) & 0xFF) +
                117 * (pixel & 0xFF) +
                0x200) >> 10;
          }
        }
        raster.setPixels(left, y, width, 1, buffer);
      }

    }
    this.left = left;
    this.top = top;
  }
```
