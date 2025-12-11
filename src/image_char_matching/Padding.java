package image_char_matching;

import image.Image;
import java.awt.Color;

/**
 * Utility class for padding images to power-of-two dimensions, splitting them
 * into smaller sub-images, and computing their brightness.
 * The methods in this class are used as part of the ASCII art generation
 * process to:
 * 1. Pad an image so that its width and height are powers of two.
 * 2. Divide a padded image into equally sized square sub-images.
 * 3. Compute the normalized grayscale brightness of an image.
 */
public class Padding {

    static final Color  WHITE = new Color(255, 255, 255);
    static final double RED_GRAY = 0.2126;
    static final double GREEN_GRAY = 0.7152;
    static final double BLUE_GRAY = 0.0722;
    static final double MAX_VALUE = 255.0;

    /**
     * Pads the given image so that its width and height become powers of two.
     * If the image already has power-of-two dimensions, the original image is returned.
     * Otherwise, a new image is created with a white background, and the original
     * image is centered inside it.
     * @param image the original image to pad
     * @return the original image if no padding is needed, or a new padded image otherwise
     */
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
                newPixels[y + yOffset][x + xOffset] = originalImage.getPixel(y, x);
            }
        }
        return new Image(newPixels, newWidth, newHeight);
    }

    /**
     * Splits an image into a grid of square sub-images based on the given resolution.
     * The image is divided into "resolution" columns, and the block size is determined
     * by width / resolution. The number of rows of sub-images is height / blockWidth.
     * Each sub-image is a blockWidth x blockWidth square.
     * @param image the image to split into sub-images
     * @param resolution the number of sub-images per row (horizontal resolution)
     * @return a 2D array of sub-images, where each sub-image is a square block
     */
    public static Image[][] subImages(Image image, int resolution) {
        int width = image.getWidth();
        int height = image.getHeight();

        int blockWidth  = width / resolution;
        int subRows = height / blockWidth;

        Image[][] subs = new Image[subRows][resolution];

        for (int row = 0; row < subRows; row++) {
            for (int col = 0; col < resolution; col++) {

                Color[][] block = new Color[blockWidth][blockWidth];

                for (int y = 0; y < blockWidth; y++) {
                    for (int x = 0; x < blockWidth; x++) {
                        block[y][x] = image.getPixel(row * blockWidth + y,
                                col * blockWidth + x);
                    }
                }
                subs[row][col] = new Image(block, blockWidth, blockWidth);
            }
        }
        return subs;
    }

    /**
     * Computes the normalized grayscale brightness of an image.
     * The brightness is computed as the weighted sum of the red, green, and blue
     * components of each pixel (using fixed coefficients), averaged over all pixels,
     * and then divided by 255 to return a value in the range [0, 1].
     * @param image the image whose brightness is to be computed
     * @return the average grayscale brightness in the range [0, 1]
     */
    public static double computeBrightness(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();

        double sumGray = 0.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = image.getPixel(y, x);
                double gray = c.getRed()   * RED_GRAY
                        + c.getGreen() * GREEN_GRAY
                        + c.getBlue()  * BLUE_GRAY;
                sumGray += gray;
            }
        }
        double avgGray = sumGray / (width * height);
        return avgGray / MAX_VALUE;
    }
}
