package ascii_art;

import image.Image;


import image_char_matching.Padding;
import image_char_matching.SubImgCharMatcher;

public class AsciiArtAlgorithm {
    private final Image image;
    private final int resolution;
    private final SubImgCharMatcher matcher;

    private Image padded;
    private Image[][] subImages;
    private double[][] brightnessCache;

    private boolean reverse = false;

    void setReverse(boolean reverse) {
        this.reverse = reverse;
    }
    public AsciiArtAlgorithm(Image image, int resolution, SubImgCharMatcher matcher) {
        this.image = image;
        this.resolution = resolution;
        this.matcher = matcher;
    }

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

                    if (reversed < min) {
                        reversed = min;
                    } else if (reversed > max) {
                        reversed = max;
                    }

                    brightness = reversed;
                }

                char c = matcher.getCharByImageBrightness(brightness);
                result[row][col] = c;
            }
        }
        return result;
    }
}