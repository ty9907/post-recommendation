package com.example.demo.tag;

import com.example.demo.tag.model.Tag;
import java.util.List;

public interface TagExtractor {
    /**
     * 从文章内容中提取标签
     * @param content 文章内容
     * @return 标签列表，按权重排序
     */
    List<Tag> extractTags(String content);

    /**
     * 从文章内容中提取指定数量的标签
     * @param content 文章内容
     * @param limit 标签数量限制
     * @return 标签列表，按权重排序
     */
    List<Tag> extractTags(String content, int limit);

    /**
     * 获取提取器名称
     * @return 提取器名称
     */
    String getName();
}