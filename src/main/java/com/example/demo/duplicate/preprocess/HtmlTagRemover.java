package com.example.demo.duplicate.preprocess;

import java.util.regex.Pattern;

/**
 * HTML/XML 标签去除器。
 */
public class HtmlTagRemover {

    private static final Pattern COMMENT_PATTERN = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern STYLE_PATTERN = Pattern.compile("<style[^>]*>.*?</style>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");

    /**
     * 去除 HTML/XML 标签和注释。
     *
     * @param text 原始文本
     * @return 去标签后的文本
     */
    public String remove(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String result = COMMENT_PATTERN.matcher(text).replaceAll(" ");
        result = SCRIPT_PATTERN.matcher(result).replaceAll(" ");
        result = STYLE_PATTERN.matcher(result).replaceAll(" ");
        return TAG_PATTERN.matcher(result).replaceAll(" ");
    }
}
