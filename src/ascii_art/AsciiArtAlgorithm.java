package ascii_art;
import image.Image;
import image_char_matching.Padding;
import image_char_matching.SubImgCharMatcher;

/**
 * Responsible for converting an Image into an ASCII-art character matrix.
 * The algorithm works as follows:
 * 1. Pads the original image so its dimensions are suitable for splitting.
 * 2. Splits the padded image into sub-images based on the given resolution.
 * 3. Computes and caches the brightness of each sub-image.
 * 4. Optionally reverses the brightness values.
 * 5. Maps each brightness value to a character using the SubImgCharMatcher.
 */
public class AsciiArtAlgorithm {
    private final Image image;
    private final int resolution;
    private final SubImgCharMatcher matcher;
    private Image padded;
    private Image[][] subImages;
    private double[][] brightnessCache;
    private boolean reverse = false;

    /**
     * Sets whether brightness values should be reversed.
     *
     * @param reverse true to invert brightness (1 - brightness), false otherwise
     */
    void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    /**
     * Creates a new AsciiArtAlgorithm instance.
     *
     * @param image the source image to convert
     * @param resolution how many sub-images the image will be divided into
     * @param matcher object used to map brightness values to characters
     */
    public AsciiArtAlgorithm(Image image, int resolution, SubImgCharMatcher matcher) {
        this.image = image;
        this.resolution = resolution;
        this.matcher = matcher;
    }

    /**
     * Runs the ASCII-art conversion algorithm.
     * Steps performed:
     * 1. Pads the image if padding has not yet been done.
     * 2. Splits the padded image into a 2D grid of sub-images if it has not already been done.
     * 3. Computes the brightness of each sub-image if brightness has not been computed yet.
     * 4. Applies optional brightness reversal.
     * 5. Uses SubImgCharMatcher to map brightness values to ASCII characters
     * @return a 2D char array representing the ASCII-art version of the image
     */
    public char[][] run() {
        if (padded == null) {
            padded = Padding.padToTwo(image);
        }
        if (subImages == null) {
            subImages = Padding.subImages(padded, resolution);
        }
        int rows = subImages.length;
        int cols = subImages[0].length;

        if (brightnessCache == null) {
            brightnessCache = new double[rows][cols];
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    Image subImg = subImages[row][col];
                    brightnessCache[row][col] = Padding.computeBrightness(subImg);
                }
            }
        }
        char[][] result = new char[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double brightness = brightnessCache[row][col];

                if (reverse) {
                    double reversed = 1.0 - brightness;
                    double min = matcher.getMinNormalizedBrightness();
                    double max = matcher.getMaxNormalizedBrightness();

                    if (reversed < min) reversed = min;
                    else if (reversed > max) reversed = max;

                    brightness = reversed;
                }
                char c = matcher.getCharByImageBrightness(brightness);
                result[row][col] = c;
            }
        }
        return result;
    }
}
