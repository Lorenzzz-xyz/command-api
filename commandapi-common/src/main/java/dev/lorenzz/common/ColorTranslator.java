package dev.lorenzz.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FunctionalInterface
public interface ColorTranslator {

    /**
     * Pattern matching hex color codes in the format {@code &#RRGGBB} or {@code &#rrggbb}.
     */
    Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    String translate(String input);

    /**
     * Translates hex color codes in the format {@code &#RRGGBB} into the
     * Minecraft internal format {@code §x§R§R§G§G§B§B}.
     *
     * @param input the input string
     * @return the string with hex color codes translated
     */
    static String translateHexCodes(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("\u00A7x");
            for (char c : hex.toCharArray()) {
                replacement.append('\u00A7').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
