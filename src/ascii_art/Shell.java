package ascii_art;
import ascii_output.AsciiOutput;
import ascii_output.ConsoleAsciiOutput;
import ascii_output.HtmlAsciiOutput;
import image.Image;
import java.io.IOException;
import java.util.List;
import image_char_matching.Padding;
import image_char_matching.SubImgCharMatcher;

/**
 * A command-line shell for generating ASCII art from an image.
 * The shell:
 * 1. Loads and pads the given image.
 * 2. Allows the user to interactively configure the character set, resolution,
 *    output mode (console or HTML) and brightness reversal.
 * 3. Executes the ASCII art generation using AsciiArtAlgorithm when requested.
 * Supported commands:
 * exit - exit the program
 * chars - print the current character set
 * add - add characters or ranges to the character set
 * remove - remove characters or ranges from the character set
 * res - show or change resolution (up/down)
 * output - switch between console and HTML output
 * asciiArt - generate and output ASCII art
 * reverse - toggle brightness reversal mode
 */
public class Shell {

    private final SubImgCharMatcher matcher;
    private int resolution = 2;
    private int imageWidth;
    private int imageHeight;
    private boolean reverseMode = false;
    private boolean htmlOutput = false;
    private static class ResolutionOutOfBoundsException extends RuntimeException {}

    /**
     * Constructs a new Shell instance with a default character set of digits 0–9.
     */
    public Shell() {
        matcher = new SubImgCharMatcher(new char[0]);
        for (char c = '0'; c <= '9'; c++) matcher.addChar(c);
    }

    /**
     * Starts the interactive shell for a given image.
     * This method:
     * 1. Tries to load the image.
     * 2. Pads the image to power-of-two dimensions and stores the padded size.
     * 3. Enters an infinite loop reading and executing user commands until "exit".
     * @param imageName path to the image file
     */
    public void run(String imageName) {

        Image image;
        try {
            image = new Image(imageName);
        } catch (IOException e) {
            System.out.println("Error: failed to load image");
            return;
        }
        Image padded = Padding.padToTwo(image);
        imageWidth = padded.getWidth();
        imageHeight = padded.getHeight();

        while (true) {
            try {
                System.out.print(">>> ");
                String input = KeyboardInput.readLine();
                if (input == null)
                    return;
                input = input.trim();
                if (input.isEmpty()) continue;

                String[] parts = input.split("\\s+");
                String command = parts[0];
                checkForValidInput(command);

                switch (command) {
                    case "exit" -> {return;}
                    case "add" -> {
                        if (parts.length < 2) {
                            System.out.println("Did not add due to incorrect format.");
                            continue;
                        }
                        String arg = parts[1];
                        try {
                            addChars(arg);
                        } catch (Exception e) {
                            System.out.println("Did not add due to incorrect format.");
                        }
                    }
                    case "remove" -> {
                        if (parts.length < 2) {
                            System.out.println("Did not remove due to incorrect format.");
                            continue;
                        }
                        String arg = parts[1];
                        try {
                            removeChars(arg);
                        } catch (Exception e) {
                            System.out.println("Did not remove due to incorrect format.");
                        }
                    }
                    case "res" -> {
                        if (parts.length == 1) {
                            System.out.println("Resolution set to " + resolution + ".");
                            continue;
                        }
                        String action = parts[1];
                        try {
                            if (action.equals("up")) {
                                increaseResolution();
                            } else if (action.equals("down")) {
                                decreaseResolution();
                            } else {
                                throw new IllegalArgumentException();
                            }
                            System.out.println("Resolution set to " + resolution);
                        } catch (ResolutionOutOfBoundsException e) {
                            System.out.println("Did not change resolution due to exceeding boundaries.");
                        } catch (Exception e) {
                            System.out.println("Did not change resolution due to incorrect format.");
                        }
                    }
                    case "chars" -> {
                        List<Character> sorted = matcher.sortChars();
                        if (sorted.isEmpty()) {
                            System.out.println();
                            continue;
                        }
                        for (char c : sorted) {
                            System.out.print(c + " ");
                        }
                        System.out.println();
                    }
                    case "reverse" -> reverseMode = !reverseMode;
                    case "output" -> {
                        if (parts.length < 2) {
                            System.out.println("Did not change output method due to incorrect format.");
                            continue;
                        }
                        String target = parts[1];

                        if (target.equals("console")) {
                            htmlOutput = false;
                        } else if (target.equals("html")) {
                            htmlOutput = true;
                        } else {
                            System.out.println("Did not change output method due to incorrect format.");
                        }
                    }
                    case "asciiArt" -> {
                        List<Character> sorted = matcher.sortChars();
                        int charAmount = sorted.size();
                        if (charAmount < 2) {
                            System.out.println("Did not execute. Charset is too small.");
                            continue;
                        }
                        AsciiArtAlgorithm algo = new AsciiArtAlgorithm(image, resolution, matcher);
                        algo.setReverse(reverseMode);
                        char[][] chars = algo.run();
                        AsciiOutput output;
                        if (htmlOutput) {
                            output = new HtmlAsciiOutput("out.html", "Courier New");
                        } else {
                            output = new ConsoleAsciiOutput();
                        }
                        output.out(chars);
                    }
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Did not execute due to incorrect command.");
            }
        }
    }

    /**
     * Verifies that the given command is one of the supported commands.
     * @param command the command name entered by the user
     * @throws IllegalArgumentException if the command is not recognized
     */
    private void checkForValidInput(String command) {
        String[] validInputs = {"exit", "chars", "add",
                "remove", "res", "output", "asciiArt", "reverse"};
        for (String validInput : validInputs) {
            if (command.equals(validInput)) {
                return;
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Adds characters to the matcher according to the given argument.
     * Supported formats:
     * - "all": add all printable ASCII characters (32–126)
     * - "space": add the space character
     * - "a-z": add a range of characters between start and end (inclusive)
     * - "x": add a single character
     * @param commandArgs the argument describing which characters to add
     * @throws IllegalArgumentException if the format is invalid or out of range
     */
    private void addChars(String commandArgs) {
        if (commandArgs.equals("all")) {
            for (char c = 32; c <= 126; c++) {
                matcher.addChar(c);
            }
            return;
        }
        else if (commandArgs.equals("space")) {
            matcher.addChar(' ');
            return;
        }
        else if (commandArgs.length() == 3 && commandArgs.charAt(1) == '-') {
            char start = commandArgs.charAt(0);
            char end = commandArgs.charAt(2);

            if (start < 32 || start > 126 || end < 32 || end > 126)
                throw new IllegalArgumentException();

            else if (start <= end) {
                for (char c = start; c <= end; c++) matcher.addChar(c);
            } else {
                for (char c = start; c >= end; c--) matcher.addChar(c);
            }
            return;
        }

        else if (commandArgs.length() == 1) {
            char c = commandArgs.charAt(0);
            if (c < 32 || c > 126) {
                throw new IllegalArgumentException();
            }
            matcher.addChar(c);
            return;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Removes characters from the matcher according to the given argument.
     * Supported formats:
     * - "all": remove all printable ASCII characters (32–126)
     * - "space": remove the space character
     * - "a-z": remove a range of characters between start and end (inclusive)
     * - "x": remove a single character
     * Invalid formats or out-of-range values either cause an exception
     * or are ignored, depending on the case.
     * @param commandArgs the argument describing which characters to remove
     * @throws IllegalArgumentException if the format is invalid in a handled case
     */
    private void removeChars(String commandArgs) {
        if (commandArgs.equals("all")) {
            for (int i = 32; i <= 126; i++) {
                char c = (char) i;
                matcher.removeChar(c);
            }
            return;
        }
        else if (commandArgs.equals("space")) {
            matcher.removeChar(' ');
            return;
        }

        else if (commandArgs.length() == 3 && commandArgs.charAt(1) == '-') {
            char start = commandArgs.charAt(0);
            char end   = commandArgs.charAt(2);
            if (start < 32 || start > 126 || end < 32 || end > 126)
                return;

            else if (start <= end) {
                for (char c = start; c <= end; c++) {
                    matcher.removeChar(c);
                }
            } else {
                for (char c = start; c >= end; c--) {
                    matcher.removeChar(c);
                }
            }
            return;
        }
        else if (commandArgs.length() == 1) {
            char c = commandArgs.charAt(0);
            if (c < 32 || c > 126) {
                return;
            }
            matcher.removeChar(c);
            return;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Doubles the current resolution, if it remains valid.
     * This method:
     * 1. Computes a new resolution by multiplying the current one by 2.
     * 2. Ensures the new resolution is a power of two.
     * 3. Checks that it is within the allowed range based on the image size.
     * @throws IllegalArgumentException if the new resolution is not a power of two
     * @throws ResolutionOutOfBoundsException if the new resolution is outside the valid range
     */
    private void increaseResolution() {
        int newRes = resolution * 2;

        if (!isPowerOfTwo(newRes)) {
            throw new IllegalArgumentException();
        }

        int min = getMinCharsInRow();
        int max = getMaxCharsInRow();

        if (newRes < min || newRes > max) {
            throw new ResolutionOutOfBoundsException();
        }
        resolution = newRes;
    }

    /**
     * Halves the current resolution, if it remains valid.
     * This method:
     * 1. Computes a new resolution by dividing the current one by 2.
     * 2. Ensures the new resolution is at least 1 and a power of two.
     * 3. Checks that it is within the allowed range based on the image size.
     * @throws IllegalArgumentException if the new resolution is invalid
     * @throws ResolutionOutOfBoundsException if the new resolution is outside the valid range
     */
    private void decreaseResolution() {
        int newRes = resolution / 2;

        if (newRes < 1 || !isPowerOfTwo(newRes)) {
            throw new IllegalArgumentException();
        }

        int min = getMinCharsInRow();
        int max = getMaxCharsInRow();

        if (newRes < min || newRes > max) {
            throw new ResolutionOutOfBoundsException();
        }
        resolution = newRes;
    }

    /**
     * Checks whether a given integer is a power of two.
     * @param n the number to test
     * @return true if n is a power of two, false otherwise
     */
    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    /**
     * Returns the minimum allowed number of characters in a row,
     * based on the image dimensions.
     * @return minimum characters per row
     */
    private int getMinCharsInRow() {
        return Math.max(1, imageWidth / imageHeight);
    }

    /**
     * Returns the maximum allowed number of characters in a row,
     * which is the padded image width.
     * @return maximum characters per row
     */
    private int getMaxCharsInRow() {
        return imageWidth;
    }

    /**
     * Program entry point.
     * Expects exactly one argument representing the path to the image file.
     * If the argument count is incorrect, prints usage information and exits.
     * @param args command-line arguments (image path expected as args[0])
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java ascii_art.Shell <image-path>");
            return;
        }
        Shell shell = new Shell();
        shell.run(args[0]);
    }
}