package io.github.zhe11catz.avltreevisualizer.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses integer values from a plain-text input file.
 * Supports space, comma, and newline separators.
 */
public final class FileImportParser {

    private FileImportParser() {
    }

    /**
     * Reads and parses integers from the given file path.
     * Invalid tokens are skipped; duplicates are preserved for caller handling.
     */
    public static List<Integer> parseFile(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        return parseContent(content);
    }

    /**
     * Parses integers from raw text content.
     */
    public static List<Integer> parseContent(String content) {
        List<Integer> values = new ArrayList<>();
        String[] tokens = content.split("[\\s,]+");

        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            try {
                values.add(Integer.parseInt(token.trim()));
            } catch (NumberFormatException ignored) {
                // Skip invalid tokens per SRS assumption
            }
        }

        return values;
    }
}
