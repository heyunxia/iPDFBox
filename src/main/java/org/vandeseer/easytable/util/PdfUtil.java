package org.vandeseer.easytable.util;


import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.util.*;

/**
 * Provides some helping functions.
 */
public class PdfUtil {

    private static final String NEW_LINE_REGEX = "\\r?\\n";

    private PdfUtil() {

    }

    /**
     * Computes the width of a String (in points).
     *
     * @param text     Text
     * @param font     Font of Text
     * @param fontSize FontSize of String
     * @return Width (in points)
     */
    public static float getStringWidth(final String text, final PDFont font, final int fontSize) {
        return Arrays.stream(text.split(NEW_LINE_REGEX))
                .max(Comparator.comparing(String::length))
                .map(x -> getWidthOfStringWithoutNewlines(x, font, fontSize))
                .orElseThrow(CouldNotDetermineStringWidthException::new);
    }

    private static float getWidthOfStringWithoutNewlines(String str, PDFont font, int fontSize) {
        try {
            return font.getStringWidth(str) * fontSize / 1000F;
        } catch (IOException exception) {
            throw new CouldNotDetermineStringWidthException(exception);
        }
    }


    /**
     * Computes the height of a font.
     *
     * @param font     Font
     * @param fontSize FontSize
     * @return Height of font
     */
    public static float getFontHeight(final PDFont font, final int fontSize) {
        return font.getFontDescriptor().getCapHeight() / 1000f * fontSize;
    }

    /**
     * Split a text into multiple lines to prevent a text-overflow.
     *
     * @param text     Text
     * @param font     Used font
     * @param fontSize Used fontSize
     * @param maxWidth Maximal width of resulting text-lines
     * @return A list of lines, where all are smaller than maxWidth
     */
    public static List<String> getOptimalTextBreakLines(final String text, final PDFont font, final int fontSize, final float maxWidth) {
        final List<String> result = new LinkedList<>();

        for (final String line : text.split(NEW_LINE_REGEX)) {
            if (PdfUtil.isLineFine(line, font, fontSize, maxWidth)) {
                result.add(line);
            } else {
                result.addAll(PdfUtil.wrapLine(line, font, fontSize, maxWidth));
            }
        }

        return result;
    }

    private static List<String> wrapLine(final String line, final PDFont font, final int fontSize, final float maxWidth) {
        if (PdfUtil.isLineFine(line, font, fontSize, maxWidth)) {
            return Collections.singletonList(line);
        }

        final List<String> resultLines = PdfUtil.splitByWords(line, font, fontSize, maxWidth);

        if (resultLines.isEmpty()) {
            resultLines.addAll(PdfUtil.splitBySize(line, font, fontSize, maxWidth));
        }

        return resultLines;
    }

    private static List<String> splitBySize(final String line, final PDFont font, final int fontSize, final float maxWidth) {
        final List<String> returnList = new ArrayList<>();

        for (int i = line.length() - 1; i > 0; i--) {
            final String fittedNewLine = line.substring(0, i) + "-";
            final String remains = line.substring(i);

            if (PdfUtil.isLineFine(fittedNewLine, font, fontSize, maxWidth)) {
                returnList.add(fittedNewLine);
                returnList.addAll(PdfUtil.wrapLine(remains, font, fontSize, maxWidth));

                break;
            }
        }

        return returnList;
    }

    private static List<String> splitByWords(final String line, final PDFont font, final int fontSize, final float maxWidth) {
        final List<String> returnList = new ArrayList<>();
        final List<String> splitBySpace = Arrays.asList(line.split(" "));

        for (int i = splitBySpace.size() - 1; i >= 0; i--) {
            final String fittedNewLine = StringUtils.join(splitBySpace.subList(0, i), " ");
            final String remains = StringUtils.join(splitBySpace.subList(i, splitBySpace.size()), " ");

            if (!StringUtils.isEmpty(fittedNewLine) && PdfUtil.isLineFine(fittedNewLine, font, fontSize, maxWidth)) {
                returnList.add(fittedNewLine);

                if (!StringUtils.equals(remains, line)) {
                    returnList.addAll(PdfUtil.wrapLine(remains, font, fontSize, maxWidth));
                }
                break;
            }
        }

        return returnList;
    }

    private static boolean isLineFine(final String line, final PDFont font, final int fontSize, final float maxWidth) {
        return maxWidth >= PdfUtil.getStringWidth(line, font, fontSize);
    }


    private static class CouldNotDetermineStringWidthException extends RuntimeException {
        CouldNotDetermineStringWidthException() {
            super();
        }

        CouldNotDetermineStringWidthException(Exception exception) {
            super(exception);
        }
    }

}
