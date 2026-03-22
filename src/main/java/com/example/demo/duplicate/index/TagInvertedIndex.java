package com.example.demo.duplicate.index;

import com.example.demo.duplicate.model.Article;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 标签倒排索引接口。
 */
public interface TagInvertedIndex {

    void addArticle(Article article);

    void removeArticle(Long articleId);

    Set<Long> findByTag(String tag);

    Set<Long> findByAnyTags(Collection<String> tags);

    Map<Long, Integer> findByAnyTagsWithCount(Collection<String> tags);

    boolean containsArticle(Long articleId);

    int size();

    void clear();
}
