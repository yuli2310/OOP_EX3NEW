package image_char_matching;
import java.util.*;



public class SubImgCharMatcher {
    private final Map<Character, Double> originalBrightness = new HashMap<>();
    private final TreeMap<Double, Character> normalizedBrightness = new TreeMap<>();
    private boolean needsNormalization = true;

    public SubImgCharMatcher(char[] charset) {
        for (char c : charset) {
            addChar(c);
        }
    }

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

    public void addChar(char c) {
        if (originalBrightness.containsKey(c)) return;

        double raw = computeRawBrightness(c);
        originalBrightness.put(c, raw);
        needsNormalization= true;
    }

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

    public double getMinNormalizedBrightness() {
        if (needsNormalization) normalizeBrightness();
        return normalizedBrightness.firstKey();
    }

    public double getMaxNormalizedBrightness() {
        if (needsNormalization) normalizeBrightness();
        return normalizedBrightness.lastKey();
    }

//    public void printChars(List<Character> chars) {
//        if (chars.isEmpty()) return;
//        for (char c : chars) {
//            System.out.print(c + " ");
//        }
//        //System.out.println();
//    }

    public List<Character> sortChars() {
        List<Character> list = new ArrayList<>(originalBrightness.keySet());
        Collections.sort(list);
        return list;
    }
}