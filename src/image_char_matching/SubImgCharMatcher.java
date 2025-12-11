package image_char_matching;

import java.util.*;

/**
 * Matches characters to image brightness values.
 * This class maintains a set of characters and assigns each one a brightness value
 * based on its visual representation. The brightness values are then normalized
 * to the range [0, 1], so that for a given image brightness, the closest matching
 * character can be selected.
 * Workflow:
 * 1. Characters are added using addChar(char) and removed using removeChar(char).
 * 2. Each character's raw brightness is computed once and stored.
 * 3. On demand, brightness values are normalized so the minimum becomes 0 and
 *    the maximum becomes 1.
 * 4. getCharByImageBrightness(double) returns the character whose normalized
 *    brightness is closest to the given brightness.
 */
public class SubImgCharMatcher {
    private final Map<Character, Double> originalBrightness = new HashMap<>();
    private final TreeMap<Double, Character> normalizedBrightness = new TreeMap<>();
    private boolean needsNormalization = true;

    /**
     * Creates a new SubImgCharMatcher with an initial set of characters.
     * Each character in the given charset is added and its brightness is computed.
     * @param charset array of characters to initialize the matcher with
     */
    public SubImgCharMatcher(char[] charset) {
        for (char c : charset) {
            addChar(c);
        }
    }

    /**
     * Returns the character whose normalized brightness is closest to the given value.
     * If the internal normalization is outdated, it is recomputed.
     * The method searches for the closest brightness in the normalized map
     * using both floor and ceiling entries.
     * @param brightness a brightness value in the range [0, 1]
     * @return the character whose brightness is closest to the given value
     */
    public char getCharByImageBrightness(double brightness) {
        if (needsNormalization) {
            normalizeBrightness();
        }

        Map.Entry<Double, Character> floor = normalizedBrightness.floorEntry(brightness);
        Map.Entry<Double, Character> ceil  = normalizedBrightness.ceilingEntry(brightness);

        if (floor == null) return ceil.getValue();
        if (ceil == null)  return floor.getValue();

        double diffFloor = Math.abs(brightness - floor.getKey());
        double diffCeil  = Math.abs(brightness - ceil.getKey());

        return (diffFloor <= diffCeil ? floor : ceil).getValue();
    }

    /**
     * Adds a character to the matcher and computes its brightness.
     * If the character is already present, it is ignored. Adding or removing
     * characters invalidates the current normalization, which will be updated
     * lazily on the next query.
     * @param c the character to add
     */
    public void addChar(char c) {
        if (originalBrightness.containsKey(c)) return;

        double raw = computeRawBrightness(c);
        originalBrightness.put(c, raw);
        needsNormalization = true;
    }

    /**
     * Removes a character from the matcher.
     * If the character does not exist in the matcher, the call has no effect.
     * Removing characters invalidates the current normalization, which will
     * be updated lazily on the next query.
     * @param c the character to remove
     */
    public void removeChar(char c) {
        if (!originalBrightness.containsKey(c)) return;
        originalBrightness.remove(c);
        needsNormalization = true;
    }

    private void normalizeBrightness() {
        normalizedBrightness.clear();

        if (originalBrightness.isEmpty()) {
            needsNormalization = false;
            return;
        }

        double maxValue = Collections.max(originalBrightness.values());
        double minValue = Collections.min(originalBrightness.values());

        for (Map.Entry<Character, Double> entry : originalBrightness.entrySet()) {
            char c = entry.getKey();
            double raw = entry.getValue();
            double norm = (raw - minValue) / (maxValue - minValue);
            normalizedBrightness.put(norm, c);
        }
        needsNormalization = false;
    }

    private double computeRawBrightness(char c) {
        boolean[][] image = CharConverter.convertToBoolArray(c);

        double trueCount = 0;
        int total = image.length * image[0].length;

        for (boolean[] row : image) {
            for (boolean pixel : row) {
                if (pixel) trueCount++;
            }
        }
        return trueCount / total;
    }

    /**
     * Returns the minimum normalized brightness of all stored characters.
     * If normalization is outdated or not yet performed, it is recomputed first.
     * @return the smallest normalized brightness value in the range [0, 1]
     */
    public double getMinNormalizedBrightness() {
        if (needsNormalization) normalizeBrightness();
        return normalizedBrightness.firstKey();
    }

    /**
     * Returns the maximum normalized brightness of all stored characters.
     * If normalization is outdated or not yet performed, it is recomputed first.
     * @return the largest normalized brightness value in the range [0, 1]
     */
    public double getMaxNormalizedBrightness() {
        if (needsNormalization) normalizeBrightness();
        return normalizedBrightness.lastKey();
    }

    /**
     * Returns a sorted list of all characters currently stored in the matcher.
     * Characters are sorted in ascending order by their character value.
     * @return a sorted list of characters
     */
    public List<Character> sortChars() {
        List<Character> list = new ArrayList<>(originalBrightness.keySet());
        Collections.sort(list);
        return list;
    }
}
