package image_char_matching;

import image.Image;
import java.awt.Color;

public class Padding {

    static final Color  WHITE = new Color(255, 255, 255);
    static final double RED_GRAY = 0.2126;
    static final double GREEN_GRAY = 0.7152;
    static final double BLUE_GRAY = 0.0722;
    static final double MAX_VALUE = 255.0;

    public static Image padToTwo(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int newWidth  = findPowOfTwo(width);
        int newHeight = findPowOfTwo(height);

        if (newWidth == width && newHeight == height) {
            return image;
        }
        return createPaddedImage(newWidth, newHeight, image);
    }

    private static int findPowOfTwo(int number) {
        if (checkIfPowerOfTwo(number)) return number;
        while (!checkIfPowerOfTwo(number)) {
            number++;
        }
        return number;
    }

    private static boolean checkIfPowerOfTwo(int number) {
        return number > 0 && (number & (number - 1)) == 0;
    }

    private static Image createPaddedImage(int newWidth, int newHeight, Image originalImage) {
        int oldWidth  = originalImage.getWidth();
        int oldHeight = originalImage.getHeight();

        Color[][] newPixels = new Color[newHeight][newWidth];

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                newPixels[y][x] = WHITE;
            }
        }

        int xOffset = (newWidth - oldWidth) / 2;
        int yOffset = (newHeight - oldHeight) / 2;

        for (int y = 0; y < oldHeight; y++) {
            for (int x = 0; x < oldWidth; x++) {
                // שינוי יחיד: להחליף את הסדר של x,y בקריאה
                newPixels[y + yOffset][x + xOffset] = originalImage.getPixel(y, x);
            }
        }
        return new Image(newPixels, newWidth, newHeight);
    }

    public static Image[][] subImages(Image image, int resolution) {
        int width = image.getWidth();
        int height = image.getHeight();

        int subCols = width / resolution;
        int subRows = height / resolution;

        Image[][] subs = new Image[subRows][subCols];

        for (int row = 0; row < subRows; row++) {
            for (int col = 0; col < subCols; col++) {

                Color[][] block = new Color[resolution][resolution];

                for (int y = 0; y < resolution; y++) {
                    for (int x = 0; x < resolution; x++) {
                        block[y][x] = image.getPixel(col * resolution + x,
                                row * resolution + y);
                    }
                }
                subs[row][col] = new Image(block, resolution, resolution);
            }
        }
        return subs;
    }

    public static double computeBrightness(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();

        double sumGray = 0.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = image.getPixel(x, y);

                double gray = c.getRed() * RED_GRAY
                        + c.getGreen() * GREEN_GRAY
                        + c.getBlue() * BLUE_GRAY;

                sumGray += gray;
            }
        }
        double avgGray = sumGray / (width * height);
        return avgGray / MAX_VALUE;
    }
}