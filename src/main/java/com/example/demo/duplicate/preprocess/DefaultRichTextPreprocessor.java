package com.example.demo.duplicate.preprocess;

/**
 * 默认富文本预处理器实现。
 */
public class DefaultRichTextPreprocessor implements RichTextPreprocessor {

    private final HtmlTagRemover htmlTagRemover;
    private final HtmlEntityDecoder htmlEntityDecoder;
    private final TextNormalizer textNormalizer;
    private final PreprocessedTextCache cache;

    public DefaultRichTextPreprocessor() {
        this(new HtmlTagRemover(), new HtmlEntityDecoder(), new TextNormalizer(), new PreprocessedTextCache());
    }

    public DefaultRichTextPreprocessor(HtmlTagRemover htmlTagRemover,
                                       HtmlEntityDecoder htmlEntityDecoder,
                                       TextNormalizer textNormalizer,
                                       PreprocessedTextCache cache) {
        this.htmlTagRemover = htmlTagRemover;
        this.htmlEntityDecoder = htmlEntityDecoder;
        this.textNormalizer = textNormalizer;
        this.cache = cache;
    }

    @Override
    public String preprocess(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return cache.getOrCompute(text, () -> normalize(decodeEntities(stripTags(text))));
    }

    @Override
    public String stripTags(String text) {
        return htmlTagRemover.remove(text);
    }

    @Override
    public String decodeEntities(String text) {
        return htmlEntityDecoder.decode(text);
    }

    @Override
    public String normalize(String text) {
        return textNormalizer.normalize(text);
    }
}
