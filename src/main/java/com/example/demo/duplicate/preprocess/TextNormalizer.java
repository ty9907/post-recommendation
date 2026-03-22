package com.example.demo.duplicate.preprocess;

import java.util.regex.Pattern;

/**
 * 文本规范化器。
 */
public class TextNormalizer {

    private static final Pattern CONTROL_CHARS_PATTERN = Pattern.compile("[\\x00-\\x1F\\x7F]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern SPECIAL_CHARS_PATTERN =
            Pattern.compile("[^\\p{IsHan}a-zA-Z0-9\\s.,;:!?()\\-_'\"/]+");

    /**
     * 规范化文本格式。
     *
     * @param text 原始文本
     * @return 规范化后的文本
     */
    public String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String result = CONTROL_CHARS_PATTERN.matcher(text).replaceAll(" ");
        result = SPECIAL_CHARS_PATTERN.matcher(result).replaceAll(" ");
        result = WHITESPACE_PATTERN.matcher(result).replaceAll(" ");
        return result.trim();
    }
}
