package com.example.demo.duplicate.preprocess;

/**
 * 富文本预处理接口。
 *
 * 定义对 HTML/XML 富文本内容进行清洗、解码和规范化的统一入口。
 */
public interface RichTextPreprocessor {

    /**
     * 执行完整预处理。
     *
     * @param text 原始文本
     * @return 预处理后的纯文本
     */
    String preprocess(String text);

    /**
     * 去除 HTML/XML 标签。
     *
     * @param text 原始文本
     * @return 去标签后的文本
     */
    String stripTags(String text);

    /**
     * 解码 HTML 实体。
     *
     * @param text 原始文本
     * @return 解码后的文本
     */
    String decodeEntities(String text);

    /**
     * 规范化文本。
     *
     * @param text 原始文本
     * @return 规范化后的文本
     */
    String normalize(String text);
}
