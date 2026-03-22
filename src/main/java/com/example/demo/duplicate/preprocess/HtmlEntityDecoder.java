package com.example.demo.duplicate.preprocess;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML 实体解码器。
 */
public class HtmlEntityDecoder {

    private static final Pattern ENTITY_PATTERN = Pattern.compile("&(#x?[0-9a-fA-F]+|[a-zA-Z]+);");

    private static final Map<String, String> NAMED_ENTITIES = Map.ofEntries(
            Map.entry("nbsp", " "),
            Map.entry("lt", "<"),
            Map.entry("gt", ">"),
            Map.entry("amp", "&"),
            Map.entry("quot", "\""),
            Map.entry("apos", "'"),
            Map.entry("copy", "(c)")
    );

    /**
     * 解码 HTML 实体。
     *
     * @param text 原始文本
     * @return 解码后的文本
     */
    public String decode(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        Matcher matcher = ENTITY_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(resolveEntity(matcher.group(1))));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String resolveEntity(String entity) {
        if (entity == null || entity.isEmpty()) {
            return "";
        }

        if (entity.startsWith("#x") || entity.startsWith("#X")) {
            return codePointToString(entity.substring(2), 16);
        }

        if (entity.startsWith("#")) {
            return codePointToString(entity.substring(1), 10);
        }

        return NAMED_ENTITIES.getOrDefault(entity, " ");
    }

    private String codePointToString(String value, int radix) {
        try {
            int codePoint = Integer.parseInt(value, radix);
            return new String(Character.toChars(codePoint));
        } catch (RuntimeException e) {
            return " ";
        }
    }
}
